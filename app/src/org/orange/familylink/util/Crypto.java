/*
 * Copyright 2012 Nikolay Elenkov (https://github.com/nelenkov/android-pbe/blob/master/LICENSE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orange.familylink.util;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.orange.familylink.BuildConfig;

import android.util.Base64;
import android.util.Log;

public class Crypto {
	private static final String TAG = Crypto.class.getSimpleName();

	private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	private static String DELIMITER = "!";

	private static int KEY_LENGTH = 256;
	// minimum values recommended by PKCS#5, increase as necessary
	private static int ITERATION_COUNT = 1000;
	private static final int PKCS5_SALT_LENGTH = 16;

	private static SecureRandom random = new SecureRandom();

	private Crypto() {
	}

	public static SecretKey deriveKeyPbkdf2(byte[] salt, String password) {
		try {
			long start, elapsed;
			if(BuildConfig.DEBUG) start = System.currentTimeMillis();
			KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
					ITERATION_COUNT, KEY_LENGTH);
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(PBKDF2_DERIVATION_ALGORITHM);
			//the output of generateSecret() is actually a PBEKey instance which does not
			//contain an initialized IV -- the Cipher object expects that from a PBEKey and
			//will throw an exception if it is not present.
			//So don't use the SecretKey produced by the factory as is,
			//but use its encoded value to create a new SecretKeySpec object
			byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

			SecretKey result = new SecretKeySpec(keyBytes, "AES");
			if(BuildConfig.DEBUG) {
				elapsed = System.currentTimeMillis() - start;
				Log.d(TAG, String.format("PBKDF2 key derivation took %d [ms].", elapsed));
			}

			return result;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] generateIv(int length) {
		byte[] b = new byte[length];
		random.nextBytes(b);

		return b;
	}

	private static byte[] generateSalt() {
		byte[] b = new byte[PKCS5_SALT_LENGTH];
		random.nextBytes(b);

		return b;
	}

	public static String encrypt(String plaintext, SecretKey key, byte[] salt) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			byte[] iv = generateIv(cipher.getBlockSize());
			if(BuildConfig.DEBUG) Log.d(TAG, "IV: " + toHex(iv));
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
			if(BuildConfig.DEBUG) Log.d(TAG, "Cipher IV: "
					+ (cipher.getIV() == null ? null : toHex(cipher.getIV())));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

			if (salt != null) {
				return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
						toBase64(iv), DELIMITER, toBase64(cipherText));
			}

			return String.format("%s%s%s", toBase64(iv), DELIMITER,
					toBase64(cipherText));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
			if(BuildConfig.DEBUG) Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] plaintext = cipher.doFinal(cipherBytes);
			String plainrStr = new String(plaintext, "UTF-8");

			return plainrStr;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encrypt(String plaintext, String password) {
		byte[] salt = generateSalt();
		SecretKey key = deriveKeyPbkdf2(salt, password);

		return encrypt(plaintext, key, salt);
	}

	public static String decrypt(String ciphertext, String password) {
		String[] fields = ciphertext.split(DELIMITER);
		if (fields.length != 3) {
			throw new IllegalArgumentException("Invalid encypted text format");
		}

		byte[] salt = fromBase64(fields[0]);
		byte[] iv = fromBase64(fields[1]);
		byte[] cipherBytes = fromBase64(fields[2]);
		SecretKey key = deriveKeyPbkdf2(salt, password);

		return decrypt(cipherBytes, key, iv);
	}

	private static String toHex(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (byte b : bytes) {
			buff.append(String.format("%02X", b));
		}
		return buff.toString();
	}

	private static String toBase64(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
	}

	private static byte[] fromBase64(String base64) {
		return Base64.decode(base64, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
	}

}
