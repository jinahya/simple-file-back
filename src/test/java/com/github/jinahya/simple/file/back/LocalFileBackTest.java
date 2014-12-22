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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static java.util.concurrent.ThreadLocalRandom.current;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBackTest {


    private static final Logger logger = getLogger(lookup().lookupClass());


    private static final String ROOT_PATH_FIELD_NAME = "localRoot";


    public static Path localRootValue(final LocalFileBack fileBack) {

        try {
            final Field field
                = LocalFileBack.class.getDeclaredField(ROOT_PATH_FIELD_NAME);
            field.setAccessible(true);
            try {
                return (Path) field.get(fileBack);
            } catch (final IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        } catch (final NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }
    }


    public static void localRootValue(final LocalFileBack fileBack,
                                      final Path localRoot) {

        try {
            final Field field
                = LocalFileBack.class.getDeclaredField(ROOT_PATH_FIELD_NAME);
            field.setAccessible(true);
            try {
                field.set(fileBack, localRoot);
            } catch (final IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        } catch (final NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }
    }


    @Test(enabled = true, invocationCount = 128)
    public static void localLeaf() throws IOException, FileBackException {

        final Path localRoot = FileBackTests.randomLocalRoot();

        final FileContext fileContext = new DefaultFileContext();

        fileContext.keyBufferSupplier(() -> FileBackTests.randomKeyBuffer());

        final Path localLeaf
            = LocalFileBack.localLeaf(localRoot, fileContext, true);
        //logger.debug("localPath: {}", localPath);
        assertTrue(Files.isDirectory(localLeaf.getParent()));
    }


    private static LocalFileBack localRootInjected(
        final LocalFileBack fileBack) {

        if (fileBack == null) {
            throw new NullPointerException("null fileBack");
        }

        return new LocalRootModule().inject(fileBack);
    }


    private static LocalFileBack localRootInjected() {

        return current().nextBoolean()
               ? new LocalRootModule().inject(LocalFileBack.class)
               : localRootInjected(new LocalFileBack());
    }


    @Test(enabled = true, invocationCount = 128)
    public void read() throws IOException, FileBackException {

        final FileBack fileBack = localRootInjected();
        final Path localRoot = LocalFileBackTest.localRootValue(
            (LocalFileBack) fileBack);

        final FileContext fileContext = new DefaultFileContext();

        final ByteBuffer keyBuffer = FileBackTests.randomKeyBuffer();
        fileContext.keyBufferSupplier(() -> keyBuffer);

        if (current().nextBoolean()) {
            fileContext.fileSuffixSupplier(() -> "txt");
        }

        final Path localLeaf
            = LocalFileBack.localLeaf(localRoot, fileContext, true);
        //logger.debug("localPath: {}", localPath);
        final byte[] expected = new byte[current().nextInt(0, 1024)];
        current().nextBytes(expected);
        Files.write(localLeaf, expected, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        //logger.debug("written: {}", Arrays.toString(expected));

        final ByteArrayOutputStream targetStream
            = new ByteArrayOutputStream(expected.length);
        fileContext.targetChannelSupplier(
            () -> Channels.newChannel(targetStream));

        fileBack.read(fileContext);

        final byte[] actual = targetStream.toByteArray();

        assertEquals(actual, expected);
    }


    @Test(enabled = true, invocationCount = 128)
    public void update() throws IOException, FileBackException {

        final LocalFileBack fileBack = localRootInjected();
        final Path localRoot = LocalFileBackTest.localRootValue(fileBack);

        final FileContext fileContext = new DefaultFileContext();

        final ByteBuffer keyBuffer = FileBackTests.randomKeyBuffer();
        fileContext.keyBufferSupplier(() -> keyBuffer);

        fileContext.localLeafConsumer(localLeaf -> {
            logger.debug("localLeaf: {}", localLeaf);
        });

        fileContext.pathNameConsumer(
            pathName -> {
                logger.debug("pathName: {}", pathName);
            });

        if (current().nextBoolean()) {
            fileContext.fileSuffixSupplier(() -> "png");
        }

        final Path localLeaf
            = LocalFileBack.localLeaf(localRoot, fileContext, true);
        logger.debug("localLeaf: {}", localLeaf);

        final byte[] expected = new byte[current().nextInt(0, 1024)];
        current().nextBytes(expected);
        //logger.debug("expected: {}", Arrays.toString(expected));
        final ByteArrayInputStream sourceStream
            = new ByteArrayInputStream(expected);
        fileContext.sourceChannelSupplier(
            () -> Channels.newChannel(sourceStream));

        fileContext.sourceCopiedConsumer(
            sourceCopied -> {
                logger.debug("bytesCopied: {}", sourceCopied);
            }
        );

        fileBack.write(fileContext);

        final byte[] actual = Files.readAllBytes(localLeaf);
        //logger.debug("actual: {}", Arrays.toString(actual));
        assertEquals(actual, expected);
    }


}
