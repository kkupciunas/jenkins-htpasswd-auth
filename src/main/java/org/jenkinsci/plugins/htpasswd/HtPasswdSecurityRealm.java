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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
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
    private final String htgroupsLocation;

    @DataBoundConstructor
    public HtPasswdSecurityRealm(String htpasswdLocation, String htgroupsLocation) {
        this.htpasswdLocation = htpasswdLocation;
        this.htgroupsLocation = htgroupsLocation;
    }

    /**
     */
    public String getHtpasswdLocation() {
        return this.htpasswdLocation;
    }

    public String getHtgroupsLocation() {
        return this.htgroupsLocation;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
        @Override
        public String getDisplayName() {
            return "htpasswd";
        }
    }

    private CachedHtFile<HtPasswdFile> cachedHtPasswdFile = null;
    private HtPasswdFile getHtPasswdFile() throws IOException, ReflectiveOperationException {
        if (cachedHtPasswdFile == null) {
            cachedHtPasswdFile = new CachedHtFile<HtPasswdFile>(this.htpasswdLocation, HtPasswdFile.class);
        }
        return cachedHtPasswdFile.get();
    }

    private CachedHtFile<HtGroupFile> cachedHtGroupsFile = null;
    private HtGroupFile getHtGroupFile() throws IOException, ReflectiveOperationException {
        if (cachedHtGroupsFile == null) {
            cachedHtGroupsFile = new CachedHtFile<HtGroupFile>(this.htgroupsLocation, HtGroupFile.class);
        }
        return cachedHtGroupsFile.get();
    }

    private static final GrantedAuthority DEFAULT_AUTHORITY[] =
            new GrantedAuthority[] { AUTHENTICATED_AUTHORITY };
    private static final GrantedAuthority GRANTED_AUTHORITY_TYPE[] =
            new GrantedAuthority[0];

    /**
     * Retrieves the array of granted authorities for the given user.
     * It will always contain at least one entry - "authenticated"
     *
     * @param username
     * @return the array of granted authorities, with at least
     */
    private GrantedAuthority[] getAuthenticatedUserGroups(final String username) {
        try {
            HtGroupFile htgroups = getHtGroupFile();
            List<String> groups = htgroups.getGroups(username);
            ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(groups.size() + 1);
            authorities.add(AUTHENTICATED_AUTHORITY);
            for (String group : groups) {
                authorities.add(new GrantedAuthorityImpl(group));
            }
            return authorities.toArray(GRANTED_AUTHORITY_TYPE);
        } catch (Exception ex) {
            return DEFAULT_AUTHORITY;
        }
    }

    private GrantedAuthority[] getUserGroups(final String username) {
        try {
            HtGroupFile htgroups = getHtGroupFile();
            List<String> groups = htgroups.getGroups(username);
            ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(groups.size());
            for (String group : groups) {
                authorities.add(new GrantedAuthorityImpl(group));
            }
            return authorities.toArray(GRANTED_AUTHORITY_TYPE);
        } catch (Exception ex) {
            return GRANTED_AUTHORITY_TYPE;
        }
    }

    @Override
    protected UserDetails authenticate(final String username, final String password)
            throws AuthenticationException {

        try {
            HtPasswdFile htpasswd = getHtPasswdFile();
            if (htpasswd.isPasswordValid(username, password)) {
                return new User(username, password,
                        true, true, true, true,
                        getAuthenticatedUserGroups(username));
            }
        } catch (Exception ex) {
            throw new BadCredentialsException(ex.getMessage());
        }
        String msg = String.format("Invalid user '%s' credentials", username);
        throw new BadCredentialsException(msg);
    }

    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException, DataAccessException {
        logger.finest("loadUserByUsername(" + username + ")");
        try {
            HtPasswdFile htpasswd = getHtPasswdFile();
            String pwEntry = htpasswd.getPassword(username);
            if (pwEntry == null)
                throw new IllegalStateException("User does not exist");

            return new User(username, "",
                    true, true, true, true,
                    getUserGroups(username));
        } catch (Exception ex) {
            String msg = String.format("Failed to load user '%s'", username);
            throw new UsernameNotFoundException(msg, ex);
        }
    }

    @Override
    public GroupDetails loadGroupByGroupname(final String groupname)
            throws UsernameNotFoundException, DataAccessException {
        logger.finest("loadGroupByGroupname(" + groupname + ")");
        try {
            HtGroupFile htgroups = getHtGroupFile();

            List<String> users = htgroups.getUsers(groupname);
            if (users != null && !users.isEmpty()) {
                return new SimpleGroup(groupname);
            }
        } catch (Exception ex) {
            String msg = String.format("Failed to load group '%s'", groupname);
            throw new UsernameNotFoundException(msg, ex);
        }
        String msg = String.format("Group '%s' not found", groupname);
        throw new UsernameNotFoundException(msg);
    }
}
