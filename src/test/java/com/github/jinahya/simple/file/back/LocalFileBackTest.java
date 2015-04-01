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
import static com.github.jinahya.simple.file.back.FileBackTests.randomFileKey;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBackTest extends AbstractModule
    implements Provider<Path> {


    @Override
    public Path get() {

        try {
            return Files.createTempDirectory("prefix");
        } catch (final IOException ioe) {
            throw new RuntimeException("failed to create temp directory", ioe);
        }
    }


    @Override
    protected void configure() {

        bind(FileBack.class).to(LocalFileBack.class);

        bind(Path.class).annotatedWith(RootPath.class).toProvider(this);

    }


    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        com.google.inject.Guice.createInjector(this).injectMembers(this);
    }


    @AfterClass
    public void afterClass() throws IOException {

        FileUtils.deleteDirectory(
            ((LocalFileBack) fileBack).rootPath().toFile());
    }


    @Test(enabled = true, invocationCount = 1)
    public void copy() throws IOException, FileBackException {

        final ByteBuffer sourceKey = randomFileKey();
        final ByteBuffer targetKey = randomFileKey(sourceKey);
        assertNotEquals(targetKey, sourceKey);

        final Path[] sourceObject_ = new Path[1];
        final Path[] targetObject_ = new Path[1];

        {
            final FileContext fileContext = new DefaultFileContext();
            fileContext.fileOperationSupplier(() -> FileOperation.COPY);
            fileContext.sourceKeySupplier(() -> sourceKey);
            fileContext.targetKeySupplier(() -> targetKey);
            fileContext.sourceObjectConsumer(so -> {
                sourceObject_[0] = (Path) so;
            });
            fileContext.targetObjectConsumer(to -> {
                targetObject_[0] = (Path) to;
            });
            fileBack.operate(fileContext);
        }

        final Path sourceObject = sourceObject_[0];
        final Path targetObject = targetObject_[0];

        assertNotNull(sourceObject);
        assertNotNull(targetObject);
        assertNotEquals(targetKey, sourceKey);

        Files.createDirectories(sourceObject.getParent());
        final int fileLength = 128;
        Files.write(sourceObject, new byte[fileLength]);

        {
            final FileContext fileContext = new DefaultFileContext();
            fileContext.fileOperationSupplier(() -> FileOperation.COPY);
            fileContext.sourceKeySupplier(() -> sourceKey);
            fileContext.targetKeySupplier(() -> targetKey);
            final Long[] sourceCopied_ = new Long[1];
            final Long[] targetCopied_ = new Long[1];
            fileContext.sourceCopiedConsumer(sc -> {
                sourceCopied_[0] = sc;
            });
            fileContext.targetCopiedConsumer(tc -> {
                targetCopied_[0] = tc;
            });
            fileBack.operate(fileContext);
            final Long sourceCopied = sourceCopied_[0];
            final Long targetCopied = targetCopied_[0];
            assertEquals(sourceCopied, Long.valueOf(fileLength));
            assertEquals(targetCopied, Long.valueOf(fileLength));
        }

        Files.delete(sourceObject);

        {
            final FileContext fileContext = new DefaultFileContext();
            fileContext.fileOperationSupplier(() -> FileOperation.COPY);
            fileContext.sourceKeySupplier(() -> sourceKey);
            fileContext.targetKeySupplier(() -> targetKey);
            final Long[] sourceCopied_ = new Long[1];
            final Long[] targetCopied_ = new Long[1];
            fileContext.sourceCopiedConsumer(sc -> {
                sourceCopied_[0] = sc;
            });
            fileContext.targetCopiedConsumer(tc -> {
                targetCopied_[0] = tc;
            });
            fileBack.operate(fileContext);
            final Long sourceCopied = sourceCopied_[0];
            final Long targetCopied = targetCopied_[0];
            assertNull(sourceCopied);
            assertNull(targetCopied);
        }
    }


    @Test(enabled = true, invocationCount = 1)
    public void delete() throws IOException, FileBackException {
    }


    @Test(enabled = true, invocationCount = 1)
    public void read() throws IOException, FileBackException {

        final ByteBuffer sourceKey = randomFileKey();
        final Path[] sourceObject_ = new Path[1];
        final Long[] sourceCopied_ = new Long[1];

        final FileContext fileContext = new DefaultFileContext();
        fileContext.fileOperationSupplier(() -> FileOperation.READ);
        fileContext.sourceKeySupplier(() -> sourceKey);
        fileContext.sourceObjectConsumer(so -> {
            sourceObject_[0] = (Path) so;
            if (ThreadLocalRandom.current().nextBoolean()) {
                try {
                    try {
                        Files.createDirectories(sourceObject_[0].getParent());
                    } catch (final FileAlreadyExistsException faee) {
                    }
                    Files.write(sourceObject_[0], new byte[128]);
                } catch (final IOException ioe) {
                    Assert.fail("failed to write data", ioe);
                }
            }
        });
        fileContext.sourceCopiedConsumer(sc -> sourceCopied_[0] = sc);

        fileContext.sourceChannelConsumer(sc -> {
        });
        fileBack.operate(fileContext);
        assertNotNull(sourceObject_[0]);
        fileContext.sourceChannelConsumer(null);

        fileContext.sourceStreamConsumer(ss -> {
        });
        fileBack.operate(fileContext);
        assertNotNull(sourceObject_[0]);
        fileContext.sourceStreamConsumer(null);

        fileContext.targetChannelSupplier(() -> {
            return Channels.newChannel(new ByteArrayOutputStream());
        });
        assertNotNull(sourceObject_[0]);
        fileContext.targetChannelSupplier(null);

        fileContext.targetStreamSupplier(() -> {
            return new ByteArrayOutputStream();
        });
        fileBack.operate(fileContext);
        assertNotNull(sourceObject_[0]);
        fileContext.targetStreamConsumer(null);
    }


    @Test(enabled = true, invocationCount = 1)
    public void write() throws IOException, FileBackException {

        final ByteBuffer targetKey = randomFileKey();
        final Path[] targetObject_ = new Path[1];

        final FileContext fileContext = new DefaultFileContext();
        fileContext.fileOperationSupplier(() -> FileOperation.WRITE);
        fileContext.targetKeySupplier(() -> targetKey);
        fileContext.targetObjectConsumer(to -> {
            targetObject_[0] = (Path) to;
        });

        final int fileLength1 = ThreadLocalRandom.current().nextInt(1024);
        fileContext.sourceChannelSupplier(() -> {
            return Channels.newChannel(
                new ByteArrayInputStream(new byte[fileLength1]));
        });
        fileBack.operate(fileContext);
        assertNotNull(targetObject_[0]);
        assertTrue(Files.isRegularFile(targetObject_[0]));
        assertEquals(targetObject_[0].toFile().length(), (long) fileLength1);
        fileContext.sourceChannelSupplier(null);

        final int fileLength2 = ThreadLocalRandom.current().nextInt(1024);
        fileContext.sourceStreamSupplier(() -> {
            return new ByteArrayInputStream(new byte[fileLength2]);
        });
        fileBack.operate(fileContext);
        assertNotNull(targetObject_[0]);
        assertTrue(Files.isRegularFile(targetObject_[0]));
        assertEquals(targetObject_[0].toFile().length(), (long) fileLength2);
        fileContext.sourceStreamSupplier(null);

        final int fileLength3 = ThreadLocalRandom.current().nextInt(1024);
        fileContext.targetChannelConsumer(tc -> {
            for (final ByteBuffer tb = ByteBuffer.wrap(new byte[fileLength3]);
                 tb.hasRemaining();) {
                try {
                    tc.write(tb);
                } catch (final IOException ioe) {
                    Assert.fail("failed to write to target channel", ioe);
                }
            }
        });
        fileBack.operate(fileContext);
        assertNotNull(targetObject_[0]);
        assertTrue(Files.isRegularFile(targetObject_[0]));
        assertEquals(targetObject_[0].toFile().length(), (long) fileLength3);
        fileContext.targetChannelConsumer(null);

        final int fileLength4 = ThreadLocalRandom.current().nextInt(1024);
        fileContext.targetStreamConsumer(ts -> {
            try {
                ts.write(new byte[fileLength4]);
            } catch (final IOException ioe) {
                Assert.fail("failed to write to target channel", ioe);
            }
        });
        fileBack.operate(fileContext);
        assertNotNull(targetObject_[0]);
        assertTrue(Files.isRegularFile(targetObject_[0]));
        assertEquals(targetObject_[0].toFile().length(), (long) fileLength4);
        fileContext.targetStreamSupplier(null);
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


    @Inject
    private FileBack fileBack;


}

