package logistica.db;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

/**
 * Una transaccion por pedido HTTP: se abre antes del handler y se cierra despues.
 * Asi un pedido que escribe en dos repositorios queda todo o nada, y los
 * controllers no tienen que saber nada de transacciones.
 */
public class TransaccionPorRequest implements WithSimplePersistenceUnit {

  public void abrir() {
    this.beginTransaction();
  }

  // los controllers atrapan sus excepciones y responden con un status de error,
  // asi que el status es lo que dice si el pedido salio bien
  public void cerrar(int statusCode) {
    try {
      if (statusCode < 400) {
        this.commitTransaction();
      } else {
        this.rollbackTransaction();
      }
    } finally {
      // el hilo lo reusa Jetty para otro pedido: si el EntityManager siguiera vivo, ese pedido
      // leeria las entidades que quedaron cacheadas de este en vez de ir a la base
      this.perThreadEntityManagerAccess().dispose();
    }
  }
}
