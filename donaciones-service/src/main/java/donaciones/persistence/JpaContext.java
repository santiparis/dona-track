package donaciones.persistence;

import io.github.flbulgarelli.jpa.extras.perthread.PerThreadEntityManagerAccess;
import io.github.flbulgarelli.jpa.extras.perthread.WithPerThreadEntityManager;

/** Contexto de producción respaldado por jpa-extras. */
public enum JpaContext implements WithPerThreadEntityManager {
    INSTANCE;

    private static final PerThreadEntityManagerAccess ENTITY_MANAGER_ACCESS =
            new PerThreadEntityManagerAccess("donaciones-production");

    @Override
    public PerThreadEntityManagerAccess perThreadEntityManagerAccess() {
        return ENTITY_MANAGER_ACCESS;
    }
}
