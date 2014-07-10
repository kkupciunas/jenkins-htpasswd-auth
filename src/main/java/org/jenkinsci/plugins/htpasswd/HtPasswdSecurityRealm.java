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

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author kesha
 */
public class HtPasswdSecurityRealm extends AbstractPasswordBasedSecurityRealm {
    private static final Logger logger = Logger.getLogger("htpasswd-security-realm");

    private final String htpasswdLocation;

    @DataBoundConstructor
    public HtPasswdSecurityRealm(String htpasswdLocation) {
        this.htpasswdLocation = htpasswdLocation;
    }

    /**
     */
    public String getHtpasswdLocation() {
        return this.htpasswdLocation;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
        @Override
        public String getDisplayName() {
            return "htpasswd";
        }
    }

    private long htpasswdLastModified = 0L;
    private HtPasswdFile htpasswdFile = null;

    private HtPasswdFile getHtPasswdFile() throws IOException {
        FileReader reader = null;
        File f = new File(this.htpasswdLocation);

        // if we cannot access the file for some reason
        // and have a cached info, return cached info
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            if (htpasswdFile != null) {
                return htpasswdFile;
            } else {
                String msg = String.format("File %s is not accessible!",
                        this.htpasswdLocation);
                throw new IOException(msg);
            }
        }

        // if modification time matches the one recorded earlier -
        // return cached info
        if ((f.lastModified() == htpasswdLastModified) && (htpasswdFile != null)) {
            return htpasswdFile;
        }

        try {
            reader = new FileReader(f);

            if (htpasswdFile == null) {
                htpasswdFile = new HtPasswdFile();
            } else {
                htpasswdFile.clear();
                logger.info("Modification detected on " +
                        htpasswdLocation + " - reloading...");
            }

            htpasswdFile.load(reader);
            htpasswdLastModified = f.lastModified();
            return htpasswdFile;
        } catch (IOException ex) {
            htpasswdLastModified = 0L;
            htpasswdFile = null;
            throw ex;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {}
        }
    }

    private static final GrantedAuthority DEFAULT_AUTHORITY[] =
            new GrantedAuthority[] { AUTHENTICATED_AUTHORITY };

    @Override
    protected UserDetails authenticate(String username, String password)
            throws AuthenticationException {

        try {
            HtPasswdFile htpasswd = getHtPasswdFile();
            if (htpasswd.isPasswordValid(username, password)) {
                return new User(username, password,
                        true, true, true, true, DEFAULT_AUTHORITY);
            }
        } catch (Exception ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
        String msg = String.format("Invalid user '%s' credentials", username);
        throw new BadCredentialsException(msg);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        logger.fine("loadUserByUsername(" + username + ")");
        try {
            HtPasswdFile htpasswd = getHtPasswdFile();
            String pwEntry = htpasswd.getPassword(username);
            if (pwEntry == null)
                throw new IllegalStateException("User does not exist");

            return new User(username, "", true, true, true, true, DEFAULT_AUTHORITY);
        } catch (IOException ex) {
            String msg = String.format("Failed to load user '%s'", username);
            throw new UsernameNotFoundException(msg, ex);
        }
    }

    @Override
    public GroupDetails loadGroupByGroupname(String groupname)
            throws UsernameNotFoundException, DataAccessException {
        logger.fine("loadGroupByGroupname(" +  groupname + ")");
        throw new UsernameNotFoundException("Group '" + groupname + "' not found.");
    }
}

