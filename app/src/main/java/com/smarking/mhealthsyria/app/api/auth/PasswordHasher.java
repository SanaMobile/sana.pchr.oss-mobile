package com.smarking.mhealthsyria.app.api.auth;

/**
 */
/* Example implementation of password hasher similar on Django's PasswordHasher
 * Requires Java8 (but should be easy to port to older JREs)
 * Currently it would work only for pbkdf2_sha256 algorithm
 *
 * Django code: https://github.com/django/django/blob/1.6.5/django/contrib/auth/hashers.py#L221
 */

import android.util.Base64;
import android.util.Log;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.UnsupportedEncodingException;

public class PasswordHasher {
    public final static Integer DEFAULT_ITERATIONS = 10000;
    public final static String algorithm = "pbkdf2_sha256";
    private static final String TAG = PasswordHasher.class.getSimpleName();

    public PasswordHasher() {}

    public static String getEncodedHash(String password, String salt, int iterations) {
        // Returns only the last part of whole encoded password
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        try {
            gen.init(password.getBytes("UTF-8"), salt.getBytes(), iterations);
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "Algorithm Not Found");
        }
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();

        byte[] hashBase64 = Base64.encode(dk, Base64.DEFAULT);
        return new String(hashBase64);
    }

    public static String encode(String password, String salt, int iterations) {
        // returns hashed password, along with algorithm, number of iterations and salt
        String hash = getEncodedHash(password, salt, iterations);
        return String.format("%s$%d$%s$%s", algorithm, iterations, salt, hash);
    }

    public static String encode(String password, String salt) {
        return encode(password, salt, DEFAULT_ITERATIONS);
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        // hashedPassword consist of: ALGORITHM, ITERATIONS_NUMBER, SALT and
        // HASH; parts are joined with dollar character ("$")
        String[] parts = hashedPassword.split("\\$");
        if (parts.length != 4) {
            // wrong hash format
            return false;
        }
        Integer iterations = Integer.parseInt(parts[1]);
        String salt = parts[2];
        String hash = encode(password, salt, iterations);

        return hash.trim().equals(hashedPassword.trim());
    }

    private static void passwordShouldMatch(String password, String expectedHash) {
        if (checkPassword(password, expectedHash)) {
            Log.e(TAG, " => OK");
        } else {
            String[] parts = expectedHash.split("\\$");
            if (parts.length != 4) {
                System.out.printf(" => Wrong hash provided: '%s'\n", expectedHash);
                return;
            }
            String salt = parts[2];
            String resultHash = encode(password, salt);
            String msg = " => Wrong! Password '%s' hash expected to be '%s' but is '%s'\n";
            System.out.printf(msg, password, expectedHash, resultHash);
        }
    }

    private static void passwordShouldNotMatch(String password, String expectedHash) {
        if (checkPassword(password, expectedHash)) {
            System.out.printf(" => Wrong (password '%s' did '%s' match but were not supposed to)\n", password, expectedHash);
        } else {
            System.out.println(" => OK (password didn't match)");
        }
    }

}