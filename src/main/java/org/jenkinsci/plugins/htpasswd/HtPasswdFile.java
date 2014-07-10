/**
 * The MIT License
 *
 * Copyright (c) 2014, Kestutis Kupciunas (aka kesha)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.htpasswd;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;

/**
 * htpasswd file parser and password checker. Supported hash types:
 * <ul>
 *  <li> MD5 ($apr1$)
 *  <li> SHA1 ({SHA})
 *  <li> CRYPT (unix crypt)
 * </ul>
 * BCRYPT has is not supported yet, because jbcrypt library uses $2a$ salt
 * revision, while apache htpasswd generates passwords using $2y$ salt revision
 *
 * @author kesha (Kestutis Kupciunas)
 */
public class HtPasswdFile extends HtFile {
    protected HashMap<String, String> entries = new HashMap<String, String>();

    @Override
    public void put(String key, String value) {
        if (key.isEmpty() || value.isEmpty()) {
            return;
        }
        entries.put(key, value);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    /**
     * Returns the hashed password entry for the given user.
     *
     * @param user user name
     * @return hashed password entry for the given user (or <code>null</code>
     * if given user does not exist)
     */
    public String getPassword(String user) {
        return entries.get(user);
    }

    /**
     * Validates the hashed password of the given user against plain text password.
     *
     * @param user user name from htpasswd file
     * @param password plain text password to validate
     * @return <code>true</code> if password matches, <code>false</code> - otherwise
     */
    public boolean isPasswordValid(String user, String password) {
        String hashed = this.getPassword(user);
        if (hashed == null)
            return false;

        Algorithm algo = getPasswordAlgorithm(hashed);

        switch (algo) {
        case MD5:
            return validateMd5Password(hashed, password);
        case SHA:
            return validateShaPassword(hashed, password);
        case CRYPT:
            return validateCryptPassword(hashed, password);
        case BCRYPT:
            // Bcrypt currently unsupported, as jbcrypt uses $2a$ salt revision
            // and htpasswd utility hashes passwords with $2y$ salt revision
            //return BCrypt.checkpw(password, hashed);
        default:
            throw new IllegalStateException("Unsupported password format: " + algo);
        }
    }

    private static boolean validateMd5Password(String hashed, String plain) {
        String result = Md5Crypt.apr1Crypt(plain, hashed);
        return hashed.equals(result);
    }

    private static boolean validateShaPassword(String hashed, String plain) {
        String result = Base64.encodeBase64String(DigestUtils.sha1(plain));
        String hashOnly = hashed.substring(5); // skip "{SHA1}"
        return hashOnly.equals(result);
    }

    private static boolean validateCryptPassword(String hashed, String plain) {
        String result = Crypt.crypt(plain, hashed);
        return hashed.equals(result);
    }

    protected enum Algorithm {
        MD5,
        SHA,
        CRYPT,
        BCRYPT,
        UNKNOWN
    }

    protected static Algorithm getPasswordAlgorithm(String passwd) {
        if (passwd == null)
            return Algorithm.UNKNOWN;

        if (passwd.startsWith("$apr1$")) {
            return Algorithm.MD5;
        }
        if (passwd.startsWith("{SHA}")) {
            return Algorithm.SHA;
        }
        if (passwd.startsWith("$2a$") || passwd.startsWith("$2x$") || passwd.startsWith("$2y$")) {
            return Algorithm.BCRYPT;
        }

        return Algorithm.CRYPT;
    }
}