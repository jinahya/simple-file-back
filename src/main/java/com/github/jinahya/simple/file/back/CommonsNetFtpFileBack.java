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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class CommonsNetFtpFileBack extends AbstractFileBack {


    @Override
    protected void copy(final FileContext fileContext)
        throws IOException, FileBackException {

        throw new UnsupportedOperationException("not implemented yet");

    }


    @Override
    protected void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        logger.debug("delete({})", fileContext);

        Objects.requireNonNull(fileContext, "null fileContext");

        final String pathName = pathName(fileContext, true, true, true);
        logger.debug("pathName: {}", pathName);

        final FTPFile fileObject;
        try {
            fileObject = ftpClient.mlistFile(pathName);
            logger.trace("file object: {}", fileObject);
            ofNullable(fileContext.sourceObjectConsumer())
                .ifPresent(soc -> soc.accept(fileObject));
            ofNullable(fileContext.targetObjectConsumer())
                .ifPresent(toc -> toc.accept(fileObject));
        } catch (final IOException ioe) {
            logger.debug("failed to create file object", ioe);
        }

        final boolean fileDeleted = ftpClient.deleteFile(pathName);
        logger.debug("file deleted: {}", fileDeleted);
    }


    @Override
    protected void read(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String pathName = pathName(fileContext, true, true, true);
        logger.debug("pathName: {}", pathName);

        final FTPFile fileObject;
        try {
            fileObject = ftpClient.mlistFile(pathName);
            logger.trace("file object: {}", fileObject);
            ofNullable(fileContext.sourceObjectConsumer())
                .ifPresent(soc -> soc.accept(fileObject));
            if (!fileObject.isFile()) {
                logger.warn("file object type: {} (not a file)",
                            fileObject.getType());
                return;
            }
        } catch (final IOException ioe) {
            logger.warn("failed to create file object", ioe);
        }

        ofNullable(fileContext.sourceChannelConsumer()).ifPresent(scc -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(pathName);
                logger.trace("source stream: {}", sourceStream);
                ofNullable(sourceStream).ifPresent(ss -> {
                    try (ReadableByteChannel sc = Channels.newChannel(ss)) {
                        scc.accept(sc);
                    } catch (final IOException ioe) {
                        logger.error("failed to close source channel?", ioe);
                    }
                });
            } catch (IOException ioe) {
                logger.error("failed to open source stream for pathName({})",
                             pathName, ioe);
            }
        });

        ofNullable(fileContext.targetChannelSupplier()
        ).map(Supplier::get).ifPresent(tc -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(pathName);
                ofNullable(sourceStream).ifPresent(ss -> {
                    try (ReadableByteChannel sc = Channels.newChannel(ss)) {
                        final long fileCopied = copy(sc, tc);
                        ofNullable(fileContext.sourceCopiedConsumer())
                            .ifPresent(scc -> scc.accept(fileCopied));
                        ofNullable(fileContext.targetCopiedConsumer())
                            .ifPresent(tcc -> tcc.accept(fileCopied));
                    } catch (final IOException ioe) {
                        logger.error("io error", ioe);
                    }
                });
            } catch (final IOException ioe) {
                logger.error("failed to open source stream for pathName({})",
                             pathName, ioe);
            }
        });
    }


    @Override
    protected void write(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String pathName = pathName(fileContext, true, false, true);
        logger.debug("pathName: {}", pathName);

        final FTPFile fileObject;
        try {
            fileObject = ftpClient.mlistFile(pathName);
            logger.debug("fileObject: {}", fileObject);
            ofNullable(fileContext.targetObjectConsumer())
                .ifPresent(toc -> toc.accept(fileObject));
        } catch (final IOException ioe) {
            logger.debug("failed to create file object", ioe);
        }

        ofNullable(fileContext.sourceChannelSupplier()
        ).map(Supplier::get).ifPresent(sc -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(pathName);
                ofNullable(targetStream).ifPresent(ts -> {
                    try (WritableByteChannel tc = Channels.newChannel(ts)) {
                        final long fileCopied = FileBackUtilities.copy(
                            sc, tc);
                        logger.debug("fileCopied: {}", fileCopied);
                        targetStream.flush();
                        final boolean commandCompleted
                            = ftpClient.completePendingCommand();
                        logger.debug("commandCompleted: {}",
                                     commandCompleted);
                        if (commandCompleted) {
                            ofNullable(fileContext.sourceCopiedConsumer())
                                .ifPresent(scc -> scc.accept(fileCopied));
                            ofNullable(fileContext.targetCopiedConsumer())
                                .ifPresent(tcc -> tcc.accept(fileCopied));
                        }
                        if (!commandCompleted) {
                            logger.error("command not completed!");
                        }
                    } catch (IOException ioe) {
                        logger.error("failed to copy file for {}", pathName,
                                     ioe);
                    }
                });
            } catch (final IOException ioe) {
                logger.error("failed to open stream for writing", ioe);
            }
        });

        ofNullable(fileContext.targetChannelConsumer()).ifPresent(tcc -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(pathName);
                ofNullable(targetStream).ifPresent(ts -> {
                    try (WritableByteChannel tc = Channels.newChannel(ts)) {
                        tcc.accept(tc);
                        targetStream.flush();
                        final boolean commandCompleted
                            = ftpClient.completePendingCommand();
                        logger.trace("commandCompleted: {}",
                                     commandCompleted);
                        if (!commandCompleted) {
                            logger.error("command failed");
                        }
                    } catch (final IOException ioe) {
                        logger.error("io error", ioe);
                    }
                });
            } catch (IOException ioe) {
                logger.error("failed to open stream for writing", ioe);
            }
        });
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


    /**
     * The <i>injected</i> FTP client to use.
     */
    @Inject
    @FtpClient
    protected transient FTPClient ftpClient;


}

