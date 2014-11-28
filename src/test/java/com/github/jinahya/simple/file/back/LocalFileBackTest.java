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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBackTest {


    private static final String ROOT_PATH_FIELD_NAME = "rootPath";


    private static final Logger logger = getLogger(lookup().lookupClass());


    public static Path getRootPathValue(final LocalFileBack fileBack) {

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


    public static void setRootPathValue(final LocalFileBack fileBack,
                                        final Path rootPathValue) {

        try {
            final Field field
                = LocalFileBack.class.getDeclaredField(ROOT_PATH_FIELD_NAME);
            field.setAccessible(true);

            try {
                field.set(fileBack, rootPathValue);
            } catch (final IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        } catch (final NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }
    }


    private static FileBack newInstance() {

        return new LocalRootPathModule().inject(LocalFileBack.class);
    }


    private static FileBack injectInstance(final LocalFileBack fileBack) {

        if (fileBack == null) {
            throw new NullPointerException("null fileBack");
        }

        return new LocalRootPathModule().inject(fileBack);
    }


    @Test
    public static void locate() throws IOException {

        final Path rootPath = Files.createTempDirectory("tmp");
        logger.debug("rootPath: {}", rootPath);

        final byte[] keyBytes = new byte[current().nextInt(1, 128)];
        current().nextBytes(keyBytes);
        logger.debug("keyBytes: {}", Arrays.toString(keyBytes));

        final Path locatedPath = LocalFileBack.locate(rootPath, keyBytes);
        logger.debug("locatedPath: {}", locatedPath);
    }


    @Test
    public void read() throws IOException {

        final FileBack fileBack = newInstance();
        final Path rootPath = getRootPathValue((LocalFileBack) fileBack);

        final byte[] keyBytes = FileBackTests.randomKeyBytes();
        final Path locatedPath = LocalFileBack.locate(rootPath, keyBytes);
        logger.debug("locatedPath: {}", locatedPath);
        final byte[] expected = new byte[current().nextInt(0, 1024)];
        current().nextBytes(expected);
        Files.write(locatedPath, expected, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        logger.debug("written: {}", Arrays.toString(expected));

        final FileContext fileContext = mock(FileContext.class);
        when(fileContext.getProperty(FileBackConstants.PROPERTY_KEY_BYTES))
            .thenReturn(keyBytes);
        final ByteArrayOutputStream targetStream
            = new ByteArrayOutputStream(expected.length);
        when(fileContext.getProperty(
            FileBackConstants.PROPERTY_TARGET_STREAM))
            .thenReturn(targetStream);

        fileBack.read(fileContext);

        final byte[] actual = targetStream.toByteArray();

        assertEquals(actual, expected);
    }


    @Test(enabled = false)
    public void write() throws IOException {

        final FileBack fileBack = newInstance();
        final Path rootPath = getRootPathValue((LocalFileBack) fileBack);

        final byte[] keyBytes = FileBackTests.randomKeyBytes();
        final Path locatedPath = LocalFileBack.locate(rootPath, keyBytes);
        logger.debug("locatedPath: {}", locatedPath);
        final byte[] expected = new byte[current().nextInt(0, 1024)];
        current().nextBytes(expected);
        Files.write(locatedPath, expected, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        logger.debug("written: {}", Arrays.toString(expected));

        final FileContext fileContext = mock(FileContext.class);
        when(fileContext.getProperty(FileBackConstants.PROPERTY_KEY_BYTES))
            .thenReturn(keyBytes);
        final ByteArrayOutputStream targetStream
            = new ByteArrayOutputStream(expected.length);
        when(fileContext.getProperty(
            FileBackConstants.PROPERTY_TARGET_STREAM))
            .thenReturn(targetStream);

        fileBack.read(fileContext);

        final byte[] actual = targetStream.toByteArray();

        assertEquals(actual, expected);
    }




}

