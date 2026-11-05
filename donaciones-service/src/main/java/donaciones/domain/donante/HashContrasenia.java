package donaciones.domain.donante;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-HMAC-SHA256 con salt individual y formato versionado. */
final class HashContrasenia {
    private static final int ITERACIONES = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final String PREFIJO = "pbkdf2-sha256$" + ITERACIONES + "$";
    private static final SecureRandom RANDOM = new SecureRandom();

    private HashContrasenia() { }

    static String generar(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return PREFIJO + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivar(password, salt));
    }

    static boolean verificar(String password, String almacenado) {
        if (password == null || password.isBlank() || almacenado == null || !almacenado.startsWith(PREFIJO)) {
            return false;
        }
        String[] partes = almacenado.substring(PREFIJO.length()).split("\\$", -1);
        if (partes.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);
            if (salt.length != SALT_BYTES || esperado.length != HASH_BITS / 8) return false;
            return MessageDigest.isEqual(esperado, derivar(password, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACIONES, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo procesar la contraseña.", e);
        } finally {
            spec.clearPassword();
        }
    }
}

