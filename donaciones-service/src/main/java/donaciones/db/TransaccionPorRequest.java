package donaciones.db;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.ArrayList;
import java.util.List;

public class TransaccionPorRequest implements WithSimplePersistenceUnit {
  private static final ThreadLocal<ContextoRequest> CONTEXTO_REQUEST = new ThreadLocal<>();

  public void abrir() {
    CONTEXTO_REQUEST.set(new ContextoRequest());
  }

  public static void iniciarSiEsNecesario() {
    ContextoRequest contexto = CONTEXTO_REQUEST.get();
    if (contexto != null && !contexto.transaccionIniciada) {
      new TransaccionPorRequest().beginTransaction();
      contexto.transaccionIniciada = true;
    }
  }

  public static void despuesDelCommit(Runnable accion) {
    ContextoRequest contexto = CONTEXTO_REQUEST.get();
    if (contexto == null) {
      accion.run();
    } else {
      contexto.accionesDespuesDelCommit.add(accion);
    }
  }

  public static void despuesDelRollback(Runnable accion) {
    ContextoRequest contexto = CONTEXTO_REQUEST.get();
    if (contexto != null) contexto.accionesDespuesDelRollback.add(accion);
  }

  public void cerrar(int statusCode) {
    ContextoRequest contexto = CONTEXTO_REQUEST.get();
    boolean confirmado = false;
    RuntimeException error = null;
    try {
      if (contexto != null && contexto.transaccionIniciada && statusCode < 400) {
        commitTransaction();
        confirmado = true;
      } else if (contexto != null && contexto.transaccionIniciada) {
        rollbackTransaction();
      }
    } catch (RuntimeException exception) {
      error = exception;
      try {
        rollbackTransaction();
      } catch (RuntimeException rollbackException) {
        exception.addSuppressed(rollbackException);
      }
    } finally {
      CONTEXTO_REQUEST.remove();
      if (perThreadEntityManagerAccess().isAttached()) {
        try {
          perThreadEntityManagerAccess().dispose();
        } catch (RuntimeException disposeException) {
          if (error == null) {
            error = disposeException;
          } else {
            error.addSuppressed(disposeException);
          }
        }
      }
    }

    if (!confirmado && contexto != null) {
      contexto.accionesDespuesDelRollback.forEach(Runnable::run);
    }

    if (error != null) {
      throw error;
    }

    if (confirmado && contexto != null) {
      RuntimeException errorCallback = null;
      for (Runnable accion : contexto.accionesDespuesDelCommit) {
        try {
          accion.run();
        } catch (RuntimeException exception) {
          if (errorCallback == null) {
            errorCallback = exception;
          } else {
            errorCallback.addSuppressed(exception);
          }
        }
      }
      if (errorCallback != null) {
        throw errorCallback;
      }
    }
  }

  private static final class ContextoRequest {
    private final List<Runnable> accionesDespuesDelCommit = new ArrayList<>();
    private final List<Runnable> accionesDespuesDelRollback = new ArrayList<>();
    private boolean transaccionIniciada;
  }
}
