/*
 * Copyright (c) 2003, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.util;

/**
 * MD5-based utility function to obfuscate passwords before network transmission.
 *
 * @author Jeremy Wohl
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Digest {
  private MD5Digest() {
  }

  /**
   * Encodes user/password/salt information in the following way: MD5(MD5(password + user) + salt)
   *
   * @param user The connecting user.
   * @param password The connecting user's password.
   * @param salt A four-salt sent by the server.
   * @return A 35-byte array, comprising the string "md5" and an MD5 digest.
   */
  public static byte[] encode(byte[] user, byte[] password, byte[] salt) {
    MessageDigest md;
    byte[] temp_digest = new byte[32];
    byte[] pass_digest;
//    byte[] hex_digest = new byte[35];
    byte[] hex_digest = new byte[32];

    try {
      md = MessageDigest.getInstance("MD5");

/*
      md.update(password);
      md.update(user);
      temp_digest = md.digest();

      bytesToHex(temp_digest, hex_digest, 0);
      md.update(hex_digest, 0, 32);
      md.update(salt);
      pass_digest = md.digest();

      bytesToHex(pass_digest, hex_digest, 3);
      hex_digest[0] = (byte) 'm';
      hex_digest[1] = (byte) 'd';
      hex_digest[2] = (byte) '5';
*/

// revised encoding MD5(username:realm:password)

byte[] out = new byte[user.length + 1 + salt.length + 1 + password.length];

System.arraycopy(user, 0, out, 0, user.length);
System.arraycopy(":".getBytes("UTF-8"), 0, out, user.length, 1);
System.arraycopy(salt, 0, out, user.length + 1, salt.length);
System.arraycopy(":".getBytes("UTF-8"), 0, out, user.length + 1 + salt.length, 1);
System.arraycopy(password, 0, out, user.length + 1 + salt.length + 1, password.length);

      md.update(out);
      md.digest(temp_digest, 0, 32);

      bytesToHex(temp_digest, hex_digest, 0);

    } catch (Exception e) {
      throw new IllegalStateException("Unable to encode password with MD5", e);
    }

    return hex_digest;
  }

  /*
   * Turn 16-byte stream into a human-readable 32-byte hex string
   */
  private static void bytesToHex(byte[] bytes, byte[] hex, int offset) {
    final char[] lookup =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    int i;
    int c;
    int j;
    int pos = offset;

    for (i = 0; i < 16; i++) {
      c = bytes[i] & 0xFF;
      j = c >> 4;
      hex[pos++] = (byte) lookup[j];
      j = (c & 0xF);
      hex[pos++] = (byte) lookup[j];
    }
  }
}
