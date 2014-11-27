/*
 * Copyright 2014 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jinahya.simple.file.back;


import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import static java.util.concurrent.ThreadLocalRandom.current;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBackTest {


    public static Path getRootFieldValue(final LocalFileBack fileBack)
        throws NoSuchFieldException, IllegalAccessException {

        final Field field = LocalFileBack.class.getDeclaredField("root");
        field.setAccessible(true);

        return (Path) field.get(fileBack);
    }


    public static void setRootFieldValue(final LocalFileBack fileBack,
                                         final Path root)
        throws NoSuchFieldException, IllegalAccessException {

        final Field field = LocalFileBack.class.getDeclaredField("root");
        field.setAccessible(true);

        field.set(fileBack, root);
    }


//    @Test
//    public static void locateWithNullIdentifier() throws IOException {
//
//        try {
//            LocalFileBack.locate(null, 1, new File("."));
//            fail("passed: locate(null, ...)");
//        } catch (final NullPointerException npe) {
//            // expected
//        }
//
//        try {
//            LocalFileBack.locate(null, 1, new File(".").toPath());
//            fail("passed: locate(null, ...)");
//        } catch (final NullPointerException npe) {
//            // expected
//        }
//    }
//
//
//    @Test
//    public static void locateWithIllegalLength() throws IOException {
//
//        try {
//            LocalFileBack.locate(
//                "identifier", Integer.MIN_VALUE | current().nextInt(),
//                new File("."));
//            fail("passed: locate(, negative, ...)");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//
//        try {
//            LocalFileBack.locate(
//                "identifier", Integer.MIN_VALUE | current().nextInt(),
//                new File(".").toPath());
//            fail("passed: locate(, negative, ...)");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//    }
//
//
//    @Test
//    public static void locateWithNullRoot() throws IOException {
//
//        try {
//            LocalFileBack.locate("identifier", 1, (File) null);
//            fail("passed: locate(, , null)");
//        } catch (final NullPointerException npe) {
//            // expected
//        }
//
//        try {
//            LocalFileBack.locate("identifier", 1, (Path) null);
//            fail("passed: locate(, , null)");
//        } catch (final NullPointerException npe) {
//            // expected
//        }
//    }
//
//
//    @Test
//    public static void locateWithNonExistingRoot() throws IOException {
//
//        final File tempDir = Files.createTempDir();
//        assertTrue(tempDir.isDirectory());
//        tempDir.deleteOnExit();
//
//        final File nonExistingRoot = new File(tempDir, "test");
//        assertFalse(nonExistingRoot.exists());
//
//        try {
//            LocalFileBack.locate("identifier", 1, nonExistingRoot);
//            fail("passed: locate(, , (non-exising))");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//
//        try {
//            LocalFileBack.locate("identifier", 1,
//                                 nonExistingRoot.toPath());
//            fail("passed: locate(, , (non-exising))");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//    }
//
//
//    @Test
//    public static void locateWithNonDirectoryRoot() throws IOException {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        final File nonDirectoryRoot = File.createTempFile("test", "test");
//        assertTrue(nonDirectoryRoot.isFile());
//        nonDirectoryRoot.deleteOnExit();
//
//        try {
//            LocalFileBack.locate("identifier", 1, nonDirectoryRoot);
//            fail("passed: locate(, , (non-directory))");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//
//        try {
//            LocalFileBack.locate("identifier", 1, nonDirectoryRoot.toPath());
//            fail("passed: locate(, , (non-directory))");
//        } catch (final IllegalArgumentException iae) {
//            // expected
//        }
//    }
//
//
//    @Test(invocationCount = 128)
//    public static void locate() throws IOException {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        final File rootFile = Files.createTempDir();
//        assertTrue(rootFile.isDirectory());
//        rootFile.deleteOnExit();
//        final File locatedFile
//            = LocalFileBack.locate("identifier", 3, rootFile);
//        logger.debug("locatedFile: {}", locatedFile);
//        assertTrue(locatedFile.getParentFile().isDirectory());
//
//        final File tempDir = Files.createTempDir();
//        assertTrue(tempDir.isDirectory());
//        tempDir.deleteOnExit();
//        final Path rootPath = tempDir.toPath();
//        final Path locatedPath
//            = LocalFileBack.locate("identifier", 3, rootPath);
//        logger.debug("locatedPath: {}", locatedPath);
//        assertTrue(locatedPath.toFile().getParentFile().isDirectory());
//    }
//
//
//    @Test(invocationCount = 128)
//    public void read() throws IOException, ReflectiveOperationException {
//
//        final File rootFile = Files.createTempDir();
//        assertTrue(rootFile.isDirectory());
//        rootFile.deleteOnExit();
//
//        final byte[] key = new byte[current().nextInt(1, 128)];
//        current().nextBytes(key);
//
//        final String identifier = FileBack.identify(
//            Channels.newChannel(new ByteArrayInputStream(key)));
//        final File located = LocalFileBack.locate(identifier, LocalFileBack.LENGTH, rootFile);
//
//        byte[] content = null;
//        if (current().nextBoolean()) {
//            content = new byte[current().nextInt(0, 128)];
//            current().nextBytes(content);
//            Files.write(content, located);
//        }
//
//        final LocalFileBack fileBack = new LocalFileBack();
//        setRootFile(fileBack, rootFile);
//
//        final ByteArrayOutputStream target = new ByteArrayOutputStream();
//
//        try {
//            fileBack.read(key, target);
//            target.flush();
//            assertEquals(target.toByteArray(), content);
//        } catch (final FileNotFoundException fnfe) {
//            if (!located.isFile()) {
//                // expected;
//            }
//            throw fnfe;
//        }
//    }
    @Test
    public static void locate() throws IOException {

        final Logger logger = getLogger(lookup().lookupClass());

        final Path rootPath = Files.createTempDirectory("tmp");
        logger.debug("rootPath: {}", rootPath);

        final byte[] keyBytes = new byte[current().nextInt(1, 128)];
        current().nextBytes(keyBytes);
        logger.debug("keyBytes: {}", Arrays.toString(keyBytes));

        final Path locatedPath = LocalFileBack.locate(rootPath, keyBytes);
        logger.debug("locatedPath: {}", locatedPath);
    }


}

