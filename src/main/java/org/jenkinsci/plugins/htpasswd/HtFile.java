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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Simple htpasswd/htgroup file format parser.
 * Lines starting with <code>#</code> are ignored as comments.
 *
 * @author kesha
 */
public abstract class HtFile {
    protected boolean clearOnLoad = true;

    public HtFile() {
        this(true);
    }

    public HtFile(boolean clearOnLoad) {
        this.clearOnLoad = clearOnLoad;
    }

    /**
     * Loads htpasswd/htgroup info from given stream.
     * Stream remains open after operation.
     *
     * @param stream stream to read htpasswd/htgroup formatted input
     * @throws IOException on any I/O error
     */
    public synchronized void load(InputStream stream) throws IOException {
        load(new InputStreamReader(stream));
    }

    /**
     * Loads htpasswd/htgroup info using given reader.
     * Reader remains open after operation.
     *
     * @param reader reader to read htpasswd/htgroup formatted input
     * @throws IOException on any I/O error
     */
    public synchronized void load(Reader reader) throws IOException {
        BufferedReader r = null;
        if (reader instanceof BufferedReader) {
            r = (BufferedReader)reader;
        } else {
            r = new BufferedReader(reader);
        }
        loadInternal(r);
    }

    public abstract void put(String key, String value);
    public abstract void clear();

    protected void loadInternal(BufferedReader reader) throws IOException {
        String line = null;
        if (this.clearOnLoad) {
            clear();
        }
        while (((line = reader.readLine()) != null)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;
            int pos = line.indexOf(':');
            if (pos == -1)
                continue;
            String key = line.substring(0, pos).trim();
            String value = line.substring(pos + 1).trim();
            put(key, value);
        }
    }
}