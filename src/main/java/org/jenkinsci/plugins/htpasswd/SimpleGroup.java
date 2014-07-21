package org.jenkinsci.plugins.htpasswd;

import hudson.security.GroupDetails;

/**
 * Simplistic implementation of {@link GroupDetails}
 */
public class SimpleGroup extends GroupDetails {
    private String name;

    public SimpleGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return (name == null) ? 0 : name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
