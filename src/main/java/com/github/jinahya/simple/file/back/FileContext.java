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
    Optional<Object> getProperty(String name);


    /**
     * Sets a property value mapped to specified {@code name}. The property will
     * be removed if {@code value} is {@code null}.
     *
     * @param name the name of the property
     * @param value the new value of the property.
     *
     * @return an optional of previous value.
     */
    Optional<Object> putProperty(String name, Object value);


    @SuppressWarnings("unchecked")
    default Supplier<byte[]> keyBytesSupplier() {

        return (Supplier<byte[]>) getProperty(
            FileBackConstants.PROPERTY_KEY_BYTES_SUPPLIER)
            .orElse(null);
    }


    default byte[] keyBytes() {

        return Optional.ofNullable(keyBytesSupplier()).orElse(() -> null).get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<byte[]> keyBytesSupplier(
        final Supplier<byte[]> keyBytesSupplier) {

        return (Supplier<byte[]>) putProperty(
            FileBackConstants.PROPERTY_KEY_BYTES_SUPPLIER, keyBytesSupplier)
            .orElse(null);
    }


    default byte[] keyBytes(final byte[] keyBytes) {

        return Optional.ofNullable(keyBytesSupplier(() -> keyBytes))
            .orElse(() -> null).get();
    }


    @SuppressWarnings("unchecked")
    default Consumer<Path> localPathConsumer() {

        return (Consumer<Path>) getProperty(
            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER)
            .orElse(null);
    }


    default void acceptLocalPath(final Supplier<Path> localPathSupplier) {

        Optional.ofNullable(localPathConsumer())
            .orElse((p) -> {
            })
            .accept(localPathSupplier.get());
    }


    @SuppressWarnings("unchecked")
    default Consumer<Path> localPathConsumer(
        final Consumer<Path> localPathConsumer) {

        return (Consumer<Path>) putProperty(
            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER, localPathConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer() {

        return (Consumer<String>) getProperty(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER)
            .orElse(null);
    }


    default void acceptPathName(final Supplier<String> pathNameSupplier) {

        Optional.ofNullable(pathNameConsumer())
            .orElse((n) -> {
            })
            .accept(pathNameSupplier.get());
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer(
        final Consumer<String> pathNameConsumer) {

        return (Consumer<String>) putProperty(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER, pathNameConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier() {

        return (Supplier<ReadableByteChannel>) getProperty(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    default ReadableByteChannel sourceChannel() {

        return Optional.ofNullable(sourceChannelSupplier())
            .orElse(() -> null).get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier(
        final Supplier<ReadableByteChannel> sourceChannelSupplier) {

        return (Supplier<ReadableByteChannel>) putProperty(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER,
            sourceChannelSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier() {

        return (Supplier<WritableByteChannel>) getProperty(
            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    default WritableByteChannel targetChannel() {

        return Optional.ofNullable(targetChannelSupplier())
            .orElse(() -> null).get();
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier(
        final Supplier<WritableByteChannel> targetChannelSupplier) {

        return (Supplier<WritableByteChannel>) putProperty(
            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER, targetChannelSupplier)
            .orElse(null);
    }


    default WritableByteChannel targetChannel(
        final WritableByteChannel targetChannel) {

        return Optional.ofNullable(targetChannelSupplier(() -> targetChannel))
            .orElse(() -> null).get();
    }


    //@SuppressWarnings("unchecked")
    default LongConsumer bytesCopiedConsumer() {

        return (LongConsumer) getProperty(
            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER)
            .orElse(null);
    }


    default void acceptBytesCopied(final LongSupplier bytesCopiedSupplier) {

        Optional.ofNullable(bytesCopiedConsumer())
            .orElse((l) -> {
            })
            .accept(bytesCopiedSupplier.getAsLong());
    }


    @SuppressWarnings("unchecked")
    default LongConsumer bytesCopiedConsumer(
        final LongConsumer bytesCopiedConsumer) {

        return (LongConsumer) putProperty(
            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER,
            bytesCopiedConsumer)
            .orElse(null);
    }


}

