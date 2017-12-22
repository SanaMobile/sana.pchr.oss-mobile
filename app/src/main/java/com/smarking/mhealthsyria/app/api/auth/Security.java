package com.smarking.mhealthsyria.app.api.auth;

import android.util.Base64;
import android.util.Log;

import com.google.zxing.ChecksumException;

import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.text.Normalizer;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-28.
 */
public class Security {

    private static final String TAG = Security.class.getSimpleName();
    public Security(){}

    /**
     *
     * @param keyBytes : 32-bytes
     * @param plainTextBytes :
     * @return
     */
    public static byte[] encrypt(byte[] keyBytes, byte[] plainTextBytes, boolean auth) throws NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException {
        byte[] ivBytes = new byte[16];
        new Random().nextBytes(ivBytes);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
        cipher.init(Cipher.ENCRYPT_MODE,  skeySpec, ivParameterSpec);

        byte[] cipherText = cipher.doFinal(plainTextBytes);

        int unixTime = (int) (System.currentTimeMillis() / 1000L);

        byte[] timeStampBytes = ByteBuffer.allocate(4).putInt(unixTime).array();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(timeStampBytes, 0, timeStampBytes.length);
        os.write(ivBytes, 0, ivBytes.length);
        os.write(cipherText, 0, cipherText.length);
        byte[] preMAC = os.toByteArray();

        os.reset();
        if(auth) {
            byte[] mac = new byte[0];
            try {
                mac = HMACSHA256_encode(keyBytes, preMAC);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            os.write(mac, 0, mac.length);
        }
        os.write(preMAC, 0, preMAC.length);

        return os.toByteArray();
    }

    public static byte[] decrypt(byte[] keyBytes, byte[] cipherText, boolean auth) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, ChecksumException {
        byte[] iv;

        if(auth) {
            byte[] mac = Arrays.copyOfRange(cipherText, 0, 32);
            byte[] timestamp = Arrays.copyOfRange(cipherText, 32, 36);
            iv = Arrays.copyOfRange(cipherText, 36, 52);
            cipherText = Arrays.copyOfRange(cipherText, 52, cipherText.length);


            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(timestamp, 0, timestamp.length);
            os.write(iv, 0, iv.length);
            os.write(cipherText, 0, cipherText.length);
            byte[] preMAC = os.toByteArray();
            byte[] ref_mac = HMACSHA256_encode(keyBytes, preMAC);
            if (!Arrays.equals(ref_mac,mac)) {
                throw ChecksumException.getChecksumInstance(new Throwable("ref_mac does not equal mac"));
            }
        }
        else{
            iv = Arrays.copyOfRange(cipherText, 4, 20);
            cipherText = Arrays.copyOfRange(cipherText, 20, cipherText.length);
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        AlgorithmParameterSpec algorithmSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE,  keySpec, algorithmSpec);

        return cipher.doFinal(cipherText);
    }

    public static byte[] HMACSHA256_encode(byte[] keyBytes, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(data);
    }


    public static String printHex(byte[] bytes, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i<end; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        String uuid = sb.toString();
        return sb.toString();
    }

    public static byte[] derivedCredential_decrypt(String password, String hashed, String check) {
        String[] parts = hashed.split("\\$");
        if (parts.length != 4) {
            // wrong hash format
            return null;
        }
        Integer iterations = Integer.parseInt(parts[1]);
        String salt = parts[2];
        Log.e(TAG, "START: " + hashed);
        byte[] key = Base64.decode(PasswordHasher.getEncodedHash(password, salt, iterations), Base64.DEFAULT);
        Log.e(TAG, "HASHED_CRED: " + new String(key));
        try {

            byte[] decrypted_key = decrypt(key, Base64.decode(parts[3], Base64.DEFAULT), true);
            Log.e(TAG, "DECRYPTED KEY: " + new String(decrypted_key, "ASCII"));
            byte[] check_encrypt = decrypt(decrypted_key, Base64.decode(check, Base64.DEFAULT), true);
            Log.e(TAG, "DECRYPTED SALT: " + new String(decrypted_key, "ASCII"));
            if (new String(check_encrypt).trim().equals(salt.trim())) {
                return decrypted_key;
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getSimpleName());
            //Log.e(TAG, e.getStackTrace().toString());
            return null;
        }
    }

    public static String normalize(String input) {
        String transformed = input.toLowerCase();
        Log.e(TAG, "INPUT " + input);
        transformed = transformed.replaceAll("\\s", "");
        transformed = Normalizer.normalize(transformed, Normalizer.Form.NFD);
        transformed = transformed.replaceAll("\\p{Mn}", "");
        Log.e(TAG, "TRANSFORMED: " + transformed);
        return transformed;
    }

}
