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


import static com.github.jinahya.simple.file.back.FileBackTests.randomFileKey;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.invoke.MethodHandles.lookup;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import static java.util.concurrent.ThreadLocalRandom.current;
import javax.inject.Inject;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
//@Guice(modules = {CommonsNetFtpFileBackModule.class, FileContextModule.class})
public class CommonsNetFtpFileBackTest extends AbstractModule {


    static final int PORT;


    static {
        PORT = current().nextInt(1024, 65536);
    }


    private static final String USERNAME = "username";


    private static final String PASSWORD = "password";


    private static FakeFtpServer SERVER;


    private static final String PATH = "path";


    private static final String FILE = "file";


    private static final String DATA = "test";


    @BeforeClass
    public static void startFtpServer() {

        final Logger logger = getLogger(lookup().lookupClass());

        SERVER = new FakeFtpServer();
        SERVER.setServerControlPort(PORT);
        logger.debug("PORT: {}", SERVER.getServerControlPort());

        final FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry("/" + PATH + "/" + FILE, DATA));
        SERVER.setFileSystem(fileSystem);

        SERVER.addUserAccount(new UserAccount(USERNAME, PASSWORD, "/"));

        SERVER.start();
        logger.debug("ftp server started");
    }


    @AfterClass
    public static void stopFtpServer() {

        final Logger logger = getLogger(lookup().lookupClass());

        SERVER.stop();
        logger.debug("ftp server stopped");

        SERVER = null;
    }


    @BeforeMethod
    public void injectFileBack() {

        fileBack = Guice.createInjector(this).getInstance(FileBack.class);

        try {
            final Field field
                = CommonsNetFtpFileBack.class.getDeclaredField("ftpClient");
            field.setAccessible(true);
            ftpClient = (FTPClient) field.get(fileBack);
        } catch (final NoSuchFieldException | SecurityException |
                       IllegalArgumentException | IllegalAccessException e) {
        }
    }


    @AfterMethod
    public void ejectFileBack() throws IOException {

        ftpClient.disconnect();
        ftpClient = null;

        fileBack = null;
    }


    @Override
    protected void configure() {

        bind(FTPClient.class)
            .annotatedWith(FtpClient.class)
            .toProvider(() -> {
                final FTPClient client = new FTPClient();
                final FTPClientConfig config = new FTPClientConfig();
                try {
                    client.connect(InetAddress.getLocalHost(), PORT);
                    logger.debug("client connected");
                } catch (final IOException e) {
                    Assert.fail("failed to connect", e);
                }
                try {
                    final boolean loggedin = client.login(USERNAME, PASSWORD);
                    logger.debug("loggedin: {}", loggedin);
                    if (!loggedin) {
                        Assert.fail("failed to login");
                    }
                } catch (final IOException ioe) {
                    Assert.fail("failed to login", ioe);
                }
                try {
                    client.setFileType(FTPClient.BINARY_FILE_TYPE);
                } catch (final IOException ioe) {
                    Assert.fail("failed to set file type to binary", ioe);
                }
                return client;
            });

        bind(FileBack.class)
            .to(CommonsNetFtpFileBack.class);
    }


    @Test
    public void test() throws IOException {

        logger.debug("injected file back: {}", fileBack);
        logger.debug("reflected ftp client: {}", ftpClient);

        for (final FTPFile file : ftpClient.listFiles()) {
            logger.debug("file: {}", file);
        }
    }


    @Test
    public void read() throws IOException, FileBackException {

        final FileContext sourceContext = new DefaultFileContext();
        sourceContext.fileOperationSupplier(() -> FileBack.FileOperation.READ);
        sourceContext.sourceKeySupplier(() -> randomFileKey());
        sourceContext.sourceObjectConsumer((sourceObject) -> {
            logger.debug("source object: {}", sourceObject);
        });
        sourceContext.sourcePathConsumer((sourcePath) -> {
            logger.debug("source path: {}", sourcePath);
        });

        fileBack.operate(sourceContext);
    }


    @Test
    public void write() throws IOException, FileBackException {

        final String[] targetPath_ = new String[1];

        final byte[] expected = "test".getBytes(StandardCharsets.US_ASCII);

        {
            final FileContext targetContext = new DefaultFileContext();
            targetContext.targetKeySupplier(() -> randomFileKey());
            targetContext.fileOperationSupplier(
                () -> FileBack.FileOperation.WRITE);
            targetContext.targetChannelConsumer(targetChannel -> {
                for (ByteBuffer targetBuffer = ByteBuffer.wrap(expected);
                     targetBuffer.hasRemaining();) {
                    try {
                        targetChannel.write(targetBuffer);
                    } catch (final IOException ioe) {
                        fail("failed to write", ioe);
                    }
                }
                logger.debug("written");
            });
            targetContext.targetObjectConsumer(
                targetObjet -> logger.debug("targetObject: {}", targetObjet));
            targetContext.targetPathConsumer(tp -> {
                logger.debug("target path: {}", tp);
                targetPath_[0] = tp;
            });
            fileBack.operate(targetContext);
        }

        final String targetPath = targetPath_[0];
        assertNotNull(targetPath);

        // read with file back
        {
            final FileContext sourceContext = new DefaultFileContext();
            sourceContext.fileOperationSupplier(
                () -> FileBack.FileOperation.READ);
            sourceContext.sourceKeySupplier(() -> randomFileKey());
            sourceContext.sourceObjectConsumer((sourceObject) -> {
                logger.debug("source object: {}", sourceObject);
            });
            sourceContext.sourcePathConsumer((sourcePath) -> {
                logger.debug("source path: {}", sourcePath);
            });
            sourceContext.sourceChannelConsumer((sc) -> {
                final byte[] actual = new byte[expected.length];
                try {
                    new DataInputStream(Channels.newInputStream(sc))
                        .readFully(actual);
                    assertEquals(actual, expected);
                } catch (final IOException ioe) {
                    fail("failed to read", ioe);
                }
            });
            fileBack.operate(sourceContext);
        }

        // read with ftp client
        {
            final InputStream fileStream
                = ftpClient.retrieveFileStream(targetPath_[0]);
            assertNotNull(fileStream);
            try {
                final byte[] actual = new byte[expected.length];
                new DataInputStream(fileStream).readFully(actual);
                assertEquals(actual, expected);
            } finally {
                fileStream.close();
            }
            final boolean commandCompleted = ftpClient.completePendingCommand();
            logger.debug("command completed: {}", commandCompleted);
        }
    }


    private final transient Logger logger = getLogger(lookup().lookupClass());


    @Inject
    private transient FileBack fileBack;


    private transient FTPClient ftpClient;


}

