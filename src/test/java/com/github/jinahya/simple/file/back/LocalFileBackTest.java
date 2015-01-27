/*
 * Copyright 2015 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
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


import com.github.jinahya.simple.file.back.FileBack.FileOperation;
import static com.github.jinahya.simple.file.back.FileBackTests.randomFileBytes;
import static com.github.jinahya.simple.file.back.FileBackTests.randomFileKey;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static java.util.concurrent.ThreadLocalRandom.current;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
@Guice(modules = {FileBackModule.class, FileContextModule.class,
                  RootPathModule.class})
public class LocalFileBackTest {


    private static final String ROOT_PATH_FIELD_NAME = "rootPath";


//    static Path rootPathFieldValue(final LocalFileBack fileBack) {
//
//        try {
//            final Field field
//                = LocalFileBack.class.getDeclaredField(ROOT_PATH_FIELD_NAME);
//            field.setAccessible(true);
//            try {
//                return (Path) field.get(fileBack);
//            } catch (final IllegalAccessException iae) {
//                throw new RuntimeException(iae);
//            }
//        } catch (final NoSuchFieldException nsfe) {
//            throw new RuntimeException(nsfe);
//        }
//    }
//
//
//    static void rootPathFieldValue(final LocalFileBack fileBack, final Path rootPath) {
//
//        try {
//            final Field field
//                = LocalFileBack.class.getDeclaredField(ROOT_PATH_FIELD_NAME);
//            field.setAccessible(true);
//            try {
//                field.set(fileBack, rootPath);
//            } catch (final IllegalAccessException iae) {
//                throw new RuntimeException(iae);
//            }
//        } catch (final NoSuchFieldException nsfe) {
//            throw new RuntimeException(nsfe);
//        }
//    }
    @Test(enabled = true, invocationCount = 1)
    public static void leafPath() throws IOException, FileBackException {

        final Path rootPath = FileBackTests.randomRootPath();
        final ByteBuffer fileKey = randomFileKey();

        final Path leafPath = LocalFileBack.leafPath(rootPath, fileKey, true);
        assertTrue(Files.isDirectory(leafPath.getParent()));
    }


    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        rootPath = (Path) FieldUtils.getField(
            LocalFileBack.class, "rootPath", true).get(fileBack);
    }


    @AfterClass
    public void afterClass() throws IOException {

        FileUtils.deleteDirectory(
            ((LocalFileBack) fileBack).rootPath().toFile());
    }


    @BeforeTest
    public void beforeTest() {

        if (fileContext instanceof AbstractFileContext) {
            ((AbstractFileContext) fileContext).properties().clear();;
        }

        fileContext.sourceObjectConsumer(
            v -> logger.trace("source object: {}", v));
        fileContext.sourceCopiedConsumer(
            v -> logger.trace("source copid: {}", v));

        fileContext.targetObjectConsumer(
            v -> logger.trace("target object: {}", v));
        fileContext.targetCopiedConsumer(
            v -> logger.trace("target copid: {}", v));
    }


    @AfterTest
    public void afterTest() {
    }


    @Test(enabled = true, invocationCount = 1)
    public void copy() throws IOException, FileBackException {

        fileContext.fileOperationSupplier(() -> FileOperation.COPY);

        final ByteBuffer sourceFileKey = randomFileKey();
        fileContext.sourceKeySupplier(() -> sourceFileKey);

        final ByteBuffer targetFileKey = randomFileKey();
        fileContext.targetKeySupplier(() -> targetFileKey);

        final Path sourceLeafPath
            = LocalFileBack.leafPath(rootPath, sourceFileKey, true);

        final byte[] fileBytes = randomFileBytes();
        final boolean fileWritten
            = Files.isRegularFile(sourceLeafPath) || current().nextBoolean();
        if (fileWritten) {
            Files.write(sourceLeafPath, fileBytes, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
            logger.trace("file written");
        }

        fileBack.operate(fileContext);
    }


    @Test(enabled = true, invocationCount = 1)
    public void delete() throws IOException, FileBackException {

        fileContext.fileOperationSupplier(() -> FileOperation.DELETE);

        final ByteBuffer fileKey = randomFileKey();
        if (current().nextBoolean()) {
            fileContext.sourceKeySupplier(() -> fileKey);
        } else {
            fileContext.targetKeySupplier(() -> fileKey);
        }

        final Path leafPath = LocalFileBack.leafPath(rootPath, fileKey, true);

        final byte[] fileBytes = randomFileBytes();
        final boolean fileWritten
            = Files.isRegularFile(leafPath) || current().nextBoolean();
        if (fileWritten) {
            Files.write(leafPath, fileBytes, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
            logger.trace("file written");
        }

        fileBack.operate(fileContext);
    }


    @Test(enabled = true, invocationCount = 1)
    public void read() throws IOException, FileBackException {

        fileContext.fileOperationSupplier(() -> FileOperation.READ);

        final ByteBuffer fileKey = randomFileKey();
        fileContext.sourceKeySupplier(() -> fileKey);

        final Path leafPath = LocalFileBack.leafPath(rootPath, fileKey, true);

        final byte[] fileBytes = randomFileBytes();
        final boolean fileWritten
            = Files.isRegularFile(leafPath) || current().nextBoolean();
        if (fileWritten) {
            Files.write(leafPath, fileBytes, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
            logger.trace("file written");
        }

        fileContext.sourceChannelConsumer(v -> {
            final byte[] actual = new byte[fileBytes.length];
            try {
                IOUtils.readFully(Channels.newInputStream(v), actual);
                if (fileWritten) {
                    assertEquals(actual, fileBytes);
                }
            } catch (final IOException ioe) {
                fail("failed to read from source channel", ioe);
            }
        });

        final ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        fileContext.targetChannelSupplier(() -> {
            return Channels.newChannel(targetStream);
        });

        fileBack.operate(fileContext);

        if (fileWritten) {
            assertEquals(targetStream.toByteArray(), fileBytes);
        }
    }


    @Test(enabled = true, invocationCount = 1)
    public void write() throws IOException, FileBackException {

        fileContext.fileOperationSupplier(() -> FileOperation.WRITE);

        final ByteBuffer fileKey = randomFileKey();
        fileContext.targetKeySupplier(() -> fileKey);

        final Path leafPath = LocalFileBack.leafPath(rootPath, fileKey, true);

        final byte[] fileBytes = randomFileBytes();
        final boolean fileWritten
            = Files.isRegularFile(leafPath) || current().nextBoolean();
        if (fileWritten) {
            Files.write(leafPath, fileBytes, StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);
            logger.trace("file written");
        }

        fileContext.sourceChannelSupplier(
            () -> Channels.newChannel(new ByteArrayInputStream(fileBytes))
        );

        fileContext.targetChannelConsumer(v -> {
            try {
                final long copied = IOUtils.copyLarge(
                    new ByteArrayInputStream(fileBytes),
                    Channels.newOutputStream(v));
            } catch (final IOException ioe) {
                logger.error("failed to copy", ioe);
            }
        });

        fileBack.operate(fileContext);

        if (fileWritten) {
            final byte[] actual = Files.readAllBytes(leafPath);
            assertEquals(actual, fileBytes);
        }
    }

//
//
//    @Test(enabled = true, invocationCount = 128)
//    public void update() throws IOException, FileBackException {
//
//        final LocalFileBack fileBack = rootPathInjected();
//        final Path localRoot = LocalFileBackTest.rootPathFieldValue(fileBack);
//
//        final FileContext fileContext = new DefaultFileContext();
//
//        final ByteBuffer keyBuffer = FileBackTests.randomFileKey();
//        fileContext.fileKeySupplier(() -> keyBuffer);
//
////        fileContext.localLeafConsumer(localLeaf -> {
////            logger.debug("localLeaf: {}", localLeaf);
////        });
//
//        fileContext.pathNameConsumer(
//            pathName -> {
//                logger.debug("pathName: {}", pathName);
//            });
//
//        if (current().nextBoolean()) {
//            fileContext.fileSuffixSupplier(() -> "png");
//        }
//
//        final Path localLeaf
//            = LocalFileBack.leafPath(localRoot, fileContext, true);
//        logger.debug("localLeaf: {}", localLeaf);
//
//        final byte[] expected = new byte[current().nextInt(0, 1024)];
//        current().nextBytes(expected);
//        final ByteArrayInputStream sourceStream
//            = new ByteArrayInputStream(expected);
//        fileContext.sourceChannelSupplier(
//            () -> Channels.newChannel(sourceStream));
//
//        fileContext.sourceCopiedConsumer(
//            sourceCopied -> {
//                logger.debug("sourceCopied: {}", sourceCopied);
//            }
//        );
//
//        fileBack.update(fileContext);
//
//        final byte[] actual = Files.readAllBytes(localLeaf);
//        assertEquals(actual, expected);
//    }

    private transient final Logger logger = getLogger(lookup().lookupClass());


    @Inject
    @Named("local")
    private FileBack fileBack;


    //@Inject
    //@RootPath
    private Path rootPath;


    @Inject
    private FileContext fileContext;


}

