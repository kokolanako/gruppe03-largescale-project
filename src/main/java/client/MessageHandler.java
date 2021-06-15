package client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class MessageHandler {

    //https://www.baeldung.com/java-rsa

    //https://www.programcreek.com/java-api-examples/?api=java.security.spec.EncodedKeySpec
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final String ALGORITHM = "RSA";

    public MessageHandler(String privKey, String pubKey) throws NoSuchAlgorithmException {
        //getBytes assumes that the keys are Base64 encoded
        byte[] publicKeyBytes = Base64.getDecoder().decode(pubKey);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privKey);
//        System.out.println(Arrays.toString(privateKeyBytes));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(this.ALGORITHM);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey=keyFactory.generatePublic(publicKeySpec);


            KeyFactory keyFactory1 = KeyFactory.getInstance(this.ALGORITHM);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            this.privateKey=keyFactory1.generatePrivate(privateKeySpec);
            System.out.println(this.privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
//        this.generateKeyPair();
    }

    public void generateKeyPair() {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        String privStringBase64 = Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
        System.out.println("code generated Private " + privStringBase64);
        this.publicKey = pair.getPublic();
        String pubStringBase64 = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        System.out.println("code generated Public: " + pubStringBase64);
    }

    public String encrypt(String clearText) {
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance(this.ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] secretMessageBytes = clearText.getBytes();
            byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
            return Base64.getEncoder().encodeToString(encryptedMessageBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        System.out.println("NEVER HERE: Sth went wrong with encryption");
        return null;
    }

    public String decrypt(String encryptedText) {

        Cipher decryptCipher = null;
        try {
            decryptCipher = Cipher.getInstance(this.ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(encryptedText.getBytes()));
            String decryptedMessage = new String(decryptedMessageBytes);
            return decryptedMessage;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        System.out.println("NEVER HERE: Sth went wrong with decryption");
        return null;
    }
}
