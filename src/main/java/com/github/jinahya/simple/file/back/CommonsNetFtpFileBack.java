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
import java.util.Objects;
import static java.util.Optional.ofNullable;
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

        copyUsing(fileContext);
    }


    @Override
    protected void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        logger.debug("delete({})", fileContext);

        Objects.requireNonNull(fileContext, "null fileContext");

        final String filePath = filePath(fileContext, true, true, true, true);
        logger.debug("file path: {}", filePath);

        try {
            ofNullable(ftpClient.mlistFile(filePath)).ifPresent(fo -> {
                logger.trace("file object: {}", fo);
                ofNullable(fileContext.sourceObjectConsumer())
                    .ifPresent(soc -> soc.accept(fo));
                ofNullable(fileContext.targetObjectConsumer())
                    .ifPresent(toc -> toc.accept(fo));
            });
        } catch (final IOException ioe) {
            logger.debug("failed to create file object", ioe);
        }

        final boolean fileDeleted = ftpClient.deleteFile(filePath);
        logger.debug("file deleted: {}", fileDeleted);
    }


    @Override
    protected void read(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String sourcePath
            = filePath(fileContext, true, false, true, false);
        logger.debug("source path: {}", sourcePath);

        try {
            ofNullable(ftpClient.mlistFile(sourcePath)).ifPresent(so -> {
                logger.debug("source object: {}", so);
                ofNullable(fileContext.sourceObjectConsumer())
                    .ifPresent(soc -> soc.accept(so));
            });
        } catch (final IOException ioe) {
            logger.debug("failed to get source object", ioe);
        }

        ofNullable(fileContext.sourceChannelConsumer()).ifPresent(scc -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(sourcePath);
                logger.debug("source stream: {}", sourceStream);
                if (sourceStream != null) {
                    try {
                        scc.accept(Channels.newChannel(sourceStream));
                        logger.debug("source channel consumer accepted");
                    } finally {
                        sourceStream.close();
                        logger.debug("source stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.error("io error", ioe);
            }
        });

        ofNullable(fileContext.sourceStreamConsumer()).ifPresent(ssc -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(sourcePath);
                logger.debug("source stream: {}", sourceStream);
                if (sourceStream != null) {
                    try {
                        ssc.accept(sourceStream);
                        logger.debug("source stream consumer acceptd");
                    } finally {
                        sourceStream.close();
                        logger.debug("source stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.error("failed to accept to source stream consumer", ioe);
            }
        });

        ofNullable(fileContext.targetChannelSupplier()).ifPresent(tcs -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(sourcePath);
                logger.debug("source stream: {}", sourceStream);
                if (sourceStream != null) {
                    try {
                        final long sourceCopied = copy(
                            Channels.newChannel(sourceStream), tcs.get());
                        logger.debug("source copied: {}", sourceCopied);
                        ofNullable(fileContext.sourceCopiedConsumer())
                            .ifPresent(scc -> scc.accept(sourceCopied));
                    } finally {
                        sourceStream.close();
                        logger.debug("source stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.debug("io error", ioe);
            }
        });

        ofNullable(fileContext.targetStreamSupplier()).ifPresent(tss -> {
            try {
                final InputStream sourceStream
                    = ftpClient.retrieveFileStream(sourcePath);
                logger.debug("source stream: {}", sourceStream);
                if (sourceStream != null) {
                    try {
                        final long sourceCopied = copy(sourceStream, tss.get());
                        logger.debug("source copied: {}", sourceCopied);
                        ofNullable(fileContext.sourceCopiedConsumer())
                            .ifPresent(scc -> scc.accept(sourceCopied));
                    } finally {
                        sourceStream.close();
                        logger.debug("source stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.debug("io error", ioe);
            }
        });
    }


    @Override
    protected void write(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String targetPath
            = filePath(fileContext, false, true, false, true);
        logger.debug("target path: {}", targetPath);

        try {
            final String directories
                = targetPath.substring(0, targetPath.lastIndexOf('/'));
            for (final String directory : directories.split("/")) {
                try {
                    if (ftpClient.changeWorkingDirectory(directory)) {
                        continue;
                    }
                    if (!ftpClient.makeDirectory(directory)) {
                        // maybe makeDirectory return true iif successfully
                        // created non-exising directory
                        // maybe another thread or session already created the
                        // directory.
                        logger.warn("failed to create directory: {}",
                                    directory);
                    }
                    if (!ftpClient.changeWorkingDirectory(directory)) {
                        throw new FileBackException(
                            "failed to change directory: " + directory);
                    }
                } catch (final IOException ioe) {
                    throw new FileBackException(
                        "failed to create directories", ioe);
                }
            }
        } catch (final IndexOutOfBoundsException ioobe) {
            // no slash in the target path?
        } finally {
            if (!ftpClient.changeWorkingDirectory("/")) {
                throw new FileBackException(
                    "failed to change working directory to /");
            }
        }

        ofNullable(fileContext.targetObjectConsumer()).ifPresent(toc -> {
            try {
                final FTPFile targetObject = ftpClient.mlistFile(targetPath);
                logger.debug("target object: {}", targetObject);
                ofNullable(targetObject).ifPresent((to) -> {
                    toc.accept(to);
                });
            } catch (final IOException ioe) {
                logger.debug("failed to create target object", ioe);
            }
        });

        ofNullable(fileContext.sourceChannelSupplier()).ifPresent(scs -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(targetPath);
                logger.debug("target stream: {}", targetStream);
                if (targetStream != null) {
                    try {
                        final long sourceCopied = copy(
                            Channels.newInputStream(scs.get()), targetStream);
                        logger.debug("source copied: {}", sourceCopied);
                        ofNullable(fileContext.sourceCopiedConsumer())
                            .ifPresent((scc) -> {
                                scc.accept(sourceCopied);
                            });
                    } finally {
                        targetStream.close();
                        logger.debug("target stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("commandCompleted: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.error("io error", ioe);
            }
        });

        ofNullable(fileContext.sourceStreamSupplier()).ifPresent(sss -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(targetPath);
                logger.debug("target stream: {}", targetStream);
                if (targetStream != null) {
                    try {
                        final long sourceCopied = copy(sss.get(), targetStream);
                        logger.debug("source copied: {}", sourceCopied);
                        ofNullable(fileContext.sourceCopiedConsumer())
                            .ifPresent((scc) -> {
                                scc.accept(sourceCopied);
                            });
                    } finally {
                        targetStream.close();
                        logger.debug("target stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("commandCompleted: {}", commandCompleted);
                }
            } catch (final IOException ioe) {
                logger.error("io error", ioe);
            }
        });

        ofNullable(fileContext.targetChannelConsumer()).ifPresent(tcc -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(targetPath);
                logger.debug("target stream: {}", targetStream);
                if (targetStream != null) {
                    try {
                        tcc.accept(Channels.newChannel(targetStream));
                        logger.debug("target channel consumer accepted");
                    } finally {
                        targetStream.close();
                        logger.debug("target stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (IOException ioe) {
                logger.error("io error", ioe);
            }
        });

        ofNullable(fileContext.targetStreamConsumer()).ifPresent(tsc -> {
            try {
                final OutputStream targetStream
                    = ftpClient.storeFileStream(targetPath);
                logger.debug("target stream: {}", targetStream);
                if (targetStream != null) {
                    try {
                        tsc.accept(targetStream);
                        logger.debug("target channel consumer accepted");
                    } finally {
                        targetStream.close();
                        logger.debug("target stream closed");
                    }
                    final boolean commandCompleted
                        = ftpClient.completePendingCommand();
                    logger.debug("command completed: {}", commandCompleted);
                }
            } catch (IOException ioe) {
                logger.error("io error", ioe);
            }
        });
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


    /**
     * The <i>injected</i> FTP client to use. The client instance must be
     * connected and logged in.
     */
    @Inject
    @FtpClient
    protected transient FTPClient ftpClient;


}

