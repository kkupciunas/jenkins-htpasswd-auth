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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * htgroup file parser.
 *
 * @author kesha (Kestutis Kupciunas)
 */
public class HtGroupFile extends HtFile {
    protected HashMap<String, List<String>> entries = new HashMap<String, List<String>>();

    @Override
    public void put(String key, String value) {
        if (key.isEmpty() || value.isEmpty()) {
            return;
        }

        ArrayList<String> users = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(value);
        while (tok.hasMoreTokens()) {
            users.add(tok.nextToken());
        }

        if (!users.isEmpty()) {
            entries.put(key, users);
        }
    }

    @Override
    public void clear() {
        entries.clear();
    }


    /**
     * Returns members of the given group.
     *
     * @param group group name
     * @return list of user names that belong to the given group (or
     * <code>null</code> if given group does not exist)
     */
    public List<String> getUsers(String group) {
        return entries.get(group);
    }

    /**
     * Returns group list that given user belongs to.
     *
     * @param user user to fetch group information for
     * @return list of groups that the given user belongs to
     */
    public List<String> getGroups(String user) {
        ArrayList<String> groups = new ArrayList<String>();

        for (Entry<String, List<String>> entry : entries.entrySet()) {
            if (entry.getValue().contains(user)){
                groups.add(entry.getKey());
            }
        }

        return groups;
    }
}