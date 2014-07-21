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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Utility container for the {@link HtFile} instance, which would maintain
 * auto-reloading cached data of the given file.
 *
 * @author kesha
 */
public class CachedHtFile<T extends HtFile> {
    private static final Logger logger = Logger.getLogger("htpasswd-htfile-cache");

    private T htFile;
    private long lastModified;
    private String fileName;
    private Class<T> clazz;

    /**
     * Creates cache instance for the given file type (htpasswd, htgroups etc).
     *
     * @param fileName name of the file to cache info for
     * @param clazz class of the specific file type handling instance
     */
    public CachedHtFile(String fileName, Class<T> clazz) {
        this.htFile = null;
        this.lastModified = 0L;
        this.fileName = fileName;
        this.clazz = clazz;
    }

    /**
     * Returns {@link HtFile} instance with most fresh info from the file that
     * is being cached. Data is reloaded on backed file modification time change.
     *
     * @return {@link HtFile} instance, ready to be queried for data
     * @throws IOException on backed file load/reload operation failures
     * @throws ReflectiveOperationException on any instance creation failure
     */
    public T get() throws IOException, ReflectiveOperationException {
        FileReader reader = null;
        File f = new File(fileName);

        // if we cannot access the file for some reason
        // and have a cached info, return cached info
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            if (htFile != null) {
                return htFile;
            } else {
                String msg = String.format("File %s is not accessible!", fileName);
                throw new IOException(msg);
            }
        }

        // if modification time matches the one recorded earlier -
        // return cached info
        if ((f.lastModified() == lastModified) && (htFile != null)) {
            return htFile;
        }

        try {
            reader = new FileReader(f);

            if (htFile == null) {
                htFile = clazz.newInstance();
            } else {
                htFile.clear();
                logger.info("Modification detected on " + fileName
                        + " - reloading...");
            }

            htFile.load(reader);
            lastModified = f.lastModified();
            return htFile;
        } catch (IOException ex) {
            lastModified = 0L;
            htFile = null;
            throw ex;
        } catch (ReflectiveOperationException ex) {
            logger.throwing("CachedHtFile", "get()", ex);
            throw ex;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
