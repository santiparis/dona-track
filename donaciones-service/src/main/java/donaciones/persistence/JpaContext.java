package donaciones.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.Consumer;
import java.util.function.Function;

/** Punto único de acceso a la unidad JPA del microservicio. */
public final class JpaContext {
    private static final String PERSISTENCE_UNIT =
            System.getProperty("donaciones.persistence.unit", "donaciones-production");
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    private static final ThreadLocal<EntityManager> ENTITY_MANAGER = ThreadLocal.withInitial(FACTORY::createEntityManager);

    private JpaContext() { }
    public static EntityManager entityManager() { return ENTITY_MANAGER.get(); }
    public static <T> T inTransaction(Function<EntityManager, T> operation) {
        EntityManager em = entityManager();
        boolean ownTransaction = !em.getTransaction().isActive();
        if (ownTransaction) em.getTransaction().begin();
        try {
            T result = operation.apply(em);
            if (ownTransaction) em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (ownTransaction && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
    public static void inTransaction(Consumer<EntityManager> operation) {
        inTransaction(em -> { operation.accept(em); return null; });
    }
}
