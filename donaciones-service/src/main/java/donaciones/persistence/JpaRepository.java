package donaciones.persistence;

import io.github.flbulgarelli.jpa.extras.WithEntityManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/** Base para repositorios con un contexto de persistencia inyectado. */
public abstract class JpaRepository implements WithEntityManager {
    private final EntityManager entityManager;

    protected JpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    /** Participa de la transacción del test o abre una transacción local. */
    protected void enTransaccion(Runnable operation) {
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownTransaction = !transaction.isActive();
        if (ownTransaction) transaction.begin();
        try {
            operation.run();
            if (ownTransaction) transaction.commit();
        } catch (RuntimeException exception) {
            if (ownTransaction && transaction.isActive()) transaction.rollback();
            throw exception;
        }
    }
}
