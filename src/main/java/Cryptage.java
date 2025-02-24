import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Classe pour crypter les données avant de les lire/écrire dans la base de
 * données.
 */
public class Cryptage {
    private static final String SECRET_KEY = "G7hC4kPzQW8nLmT2"; // Clé de 16 caractères pour AES-128
    
    /**
     * Constructeur par défaut de la classe Cryptage.
     */
    public Cryptage() {
        // Constructeur par défaut
    }

    /**
     * Chiffre une chaîne de texte en utilisant AES.
     *
     * @param data Texte à chiffrer
     * @return Texte chiffré en Base64
     * @throws Exception Si une erreur de chiffrement survient
     */
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Déchiffre une chaîne chiffrée en utilisant AES.
     *
     * @param encryptedData Texte chiffré en Base64
     * @return Texte déchiffré
     * @throws Exception Si une erreur de déchiffrement survient
     */
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
