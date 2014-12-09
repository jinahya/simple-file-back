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


import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public interface FileContext {


    /**
     * Returns the property value mapped to specified {@code name}.
     *
     * @param name the name of the property
     *
     * @return an optional of the value of property mapped to specified
     * {@code name}.
     */
    Optional<Object> property(String name);


    default <T> Optional<T> property(final String name, final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable(type.cast(property(name).orElse(null)));
    }


    @SuppressWarnings("unchecked")
    default <T> Optional<Supplier<T>> propertyOfSupplier(final String name,
                                                         final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable((Supplier<T>) property(name).orElse(null));
    }


    @SuppressWarnings("unchecked")
    default <T> Optional<Consumer<T>> propertyOfConsumer(final String name,
                                                         final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable((Consumer<T>) property(name).orElse(null));
    }


    /**
     * Sets a property value mapped to specified {@code name}. The property will
     * be removed if {@code value} is {@code null}.
     *
     * @param name the name of the property
     * @param value the new value of the property; {@code null} for removing.
     *
     * @return an optional of previous value.
     */
    Optional<Object> property(String name, Object value);


    default <T> Optional<T> property(final String name, final T value,
                                     final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable(
            type.cast(property(name, value).orElse(null)));
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> keyBufferSupplier() {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER)
            .orElse(null);
    }


    default ByteBuffer keyBuffer() {

        return Optional.ofNullable(keyBufferSupplier())
            .orElse(() -> null)
            .get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> keyBufferSupplier(
        final Supplier<ByteBuffer> keyBytesSupplier) {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER, keyBytesSupplier)
            .orElse(null);
    }


    default ByteBuffer keyBuffer(final ByteBuffer keyBuffer) {

        return Optional.ofNullable(
            keyBufferSupplier(keyBuffer == null ? null : () -> keyBuffer))
            .orElse(() -> null)
            .get();
    }


    @SuppressWarnings("unchecked")
    default Consumer<Path> localPathConsumer() {

        return (Consumer<Path>) property(
            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER)
            .orElse(null);
    }


    default void acceptLocalPath(final Supplier<Path> localPathSupplier) {

        Optional.ofNullable(localPathConsumer())
            .orElse(localPath -> {
            })
            .accept(localPathSupplier.get());
    }


    @SuppressWarnings("unchecked")
    default Consumer<Path> localPathConsumer(
        final Consumer<Path> localPathConsumer) {

        return (Consumer<Path>) property(
            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER, localPathConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer() {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER)
            .orElse(null);
    }


    default void acceptPathName(final Supplier<String> pathNameSupplier) {

        Optional.ofNullable(pathNameConsumer())
            .orElse(pathName -> {
            })
            .accept(pathNameSupplier.get());
    }


    default void acceptPathName(final String pathName) {

        acceptPathName(() -> pathName);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer(
        final Consumer<String> pathNameConsumer) {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER, pathNameConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier() {

        return (Supplier<ReadableByteChannel>) property(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    default ReadableByteChannel sourceChannel() {

        return Optional.ofNullable(sourceChannelSupplier())
            .orElse(() -> null)
            .get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier(
        final Supplier<ReadableByteChannel> sourceChannelSupplier) {

        return (Supplier<ReadableByteChannel>) property(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER,
            sourceChannelSupplier)
            .orElse(null);
    }


    default ReadableByteChannel sourceChannel(
        final ReadableByteChannel sourceChannel) {

        return Optional.ofNullable(sourceChannelSupplier(
            sourceChannel == null ? null : () -> sourceChannel))
            .orElse(() -> null)
            .get();

    }


    default ReadableByteChannel sourceChannel(final InputStream sourceStream) {

        return sourceChannel(Channels.newChannel(sourceStream));
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier() {

        return (Supplier<WritableByteChannel>) property(
            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    default WritableByteChannel targetChannel() {

        return Optional.ofNullable(targetChannelSupplier())
            .orElse(() -> null)
            .get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier(
        final Supplier<WritableByteChannel> targetChannelSupplier) {

        return (Supplier<WritableByteChannel>) property(
            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER,
            targetChannelSupplier)
            .orElse(null);
    }


    /**
     *
     * @param targetChannel
     *
     * @return previously mapped value possibly {@code null}
     */
    default WritableByteChannel targetChannel(
        final WritableByteChannel targetChannel) {

        return Optional.ofNullable(targetChannelSupplier(
            targetChannel == null ? null : () -> targetChannel))
            .orElse(() -> null)
            .get();
    }


    default WritableByteChannel targetChannel(final OutputStream targetStream) {

        return targetChannel(Channels.newChannel(targetStream));
    }


    /**
     *
     * @return the property value mapped to
     * {@link FileBackConstants#PROPERTY_BYTES_COPIED_CONSUMER} or {@code null}
     * if no mappings found
     */
    default LongConsumer bytesCopiedConsumer() {

        return (LongConsumer) property(
            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER)
            .orElse(null);
    }


    default void acceptBytesCopied(final LongSupplier bytesCopiedSupplier) {

        Optional.ofNullable(bytesCopiedConsumer())
            .orElse(bytesCopied -> {
            })
            .accept(bytesCopiedSupplier.getAsLong());
    }


    default void acceptBytesCopied(final long bytesCopied) {

        acceptBytesCopied(() -> bytesCopied);
    }


    @SuppressWarnings("unchecked")
    default LongConsumer bytesCopiedConsumer(
        final LongConsumer bytesCopiedConsumer) {

        return (LongConsumer) property(
            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER,
            bytesCopiedConsumer)
            .orElse(null);
    }


}

