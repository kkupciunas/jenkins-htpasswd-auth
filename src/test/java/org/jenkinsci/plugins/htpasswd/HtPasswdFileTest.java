package org.jenkinsci.plugins.htpasswd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HtPasswdFileTest {
    private static final String INPUT1 =
            "empty:\n" +
            "ubnt:$apr1$z.ii9bda$5iZZ8QGI3IZSONip9.jiF1\n" +
            "ubnt-md5:$apr1$e2Os.H4I$Oa4/Wm3KmI0hTXJAuUoeS/\n" +
            "ubnt-sha:{SHA}tecByS63TeTWDNwG80nkzwCdrWU=\n" +
            "ubnt-crypt:RtK6w4Y3jP2C.\n";

    private StringReader reader;
    private StringReader emptyReader;
    private HtPasswdFile htpasswd;
    private final Reader badReader = new Reader() {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            throw new IOException();
        }
        @Override
        public void close() throws IOException {
            throw new IOException();
        }
    };

    @Before
    public void setUp() throws Exception {
        reader = new StringReader(INPUT1);
        emptyReader = new StringReader("");
        htpasswd = new HtPasswdFile();
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        emptyReader.close();
    }

    @Test
    public final void testInput1() throws IOException {
        htpasswd.load(reader);

        assertNull(htpasswd.getPassword("unknown"));
        assertNull(htpasswd.getPassword("empty"));
        assertNotNull(htpasswd.getPassword("ubnt"));

        assertTrue(htpasswd.isPasswordValid("ubnt", "ubnt"));
        assertTrue(htpasswd.isPasswordValid("ubnt-md5", "ubnt"));
        assertTrue(htpasswd.isPasswordValid("ubnt-sha", "ubnt"));
        assertTrue(htpasswd.isPasswordValid("ubnt-crypt", "ubnt"));

        assertFalse(htpasswd.isPasswordValid("ubnt", "xxx"));
        assertFalse(htpasswd.isPasswordValid("ubnt-md5", "xxx"));
        assertFalse(htpasswd.isPasswordValid("ubnt-sha", "xxx"));
        assertFalse(htpasswd.isPasswordValid("ubnt-crypt", "xxx"));
    }

    @Test
    public final void testClearOnLoad() throws IOException {
        htpasswd.load(reader);
        assertNotEquals(htpasswd.entries.size(), 0);
        htpasswd.load(emptyReader);
        assertEquals(htpasswd.entries.size(), 0);
    }

    @Test(expected = NullPointerException.class)
    public final void testNullStream() throws IOException {
        htpasswd.load((InputStream)null);
    }

    @Test(expected = NullPointerException.class)
    public final void testNullReader() throws IOException {
        htpasswd.load((Reader)null);
    }

    @Test(expected = IOException.class)
    public final void testBadReader() throws IOException {
        htpasswd.load(badReader);
    }
}
