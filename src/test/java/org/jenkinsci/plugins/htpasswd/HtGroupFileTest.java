package org.jenkinsci.plugins.htpasswd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HtGroupFileTest {
    private static final String INPUT1 =
            "empty:\n" +
            "group2: root\n" +
            "group3: user1   user2     user3\n" +
            "group4:user4\n" +
            "group5: root user2\n" +
            "admin: root\n" +
            "empty2 : \n";

    private StringReader reader;
    private StringReader emptyReader;
    private HtGroupFile htgroup;

    @Before
    public void setUp() throws Exception {
        reader = new StringReader(INPUT1);
        emptyReader = new StringReader("");
        htgroup = new HtGroupFile();
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        emptyReader.close();
        htgroup.clear();
    }

    @Test
    public final void testInput1() throws IOException{
        htgroup.load(reader);
        assertNull(htgroup.getUsers("empty"));
        assertNull(htgroup.getUsers("empty2"));
        assertNull(htgroup.getUsers("unknown"));

        assertEquals(htgroup.getUsers("group2").size(), 1);
        assertEquals(htgroup.getUsers("group3").size(), 3);
        assertEquals(htgroup.getUsers("group4").size(), 1);

        List<String> users = htgroup.getUsers("group2");
        assertTrue(users.contains("root"));
        assertFalse(users.contains("user1"));

        users = htgroup.getUsers("group3");
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
        assertTrue(users.contains("user3"));
        assertFalse(users.contains("root"));

        List<String> groups = htgroup.getGroups("root");
        assertTrue(groups.contains("admin"));
        assertTrue(groups.contains("group2"));
        assertTrue(groups.contains("group5"));
        assertFalse(groups.contains("group3"));
        assertFalse(groups.contains("group4"));

        groups = htgroup.getGroups("user2");
        assertTrue(groups.contains("group3"));
        assertTrue(groups.contains("group5"));
        assertFalse(groups.contains("admin"));

        groups = htgroup.getGroups("unknown");
        assertNotNull(groups);
        assertEquals(groups.size(), 0);
    }
}
