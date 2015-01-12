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


import com.github.jinahya.simple.file.back.FileBack.FileOperation;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public interface FileContext {


    public static enum PropertyKey {


        FILE_OPERATION_SUPPLIER,
        PATH_NAME_CONSUMER,
        PATH_NAME_SUPPLIER,
        SOURCE_CHANNEL_CONSUMER,
        SOURCE_CHANNEL_SUPPLIER,
        SOURCE_COPIED_CONSUMER,
        SOURCE_OBJECT_CONSUMER,
        SOURCE_KEY_SUPPLIER,
        TARGET_KEY_SUPPLIER,
        TARGET_CHANNEL_CONSUMER,
        TARGET_CHANNEL_SUPPLIER,
        TARGET_COPIED_CONSUMER,
        TARGET_OBJECT_CONSUMER


    }


    Optional<Object> property(PropertyKey propertyKey);


    default <T> Optional<T> property(final PropertyKey key,
                                     final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return ofNullable(type.cast(property(key).orElse(null)));
    }


    Optional<Object> property(PropertyKey key, Object value);


    default <T> Optional<T> property(final PropertyKey key, final T value,
                                     final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return ofNullable(type.cast(property(key, value).orElse(null)));
    }


//    @SuppressWarnings("unchecked")
//    default Supplier<ByteBuffer> fileKeySupplier() {
//
//        return (Supplier<ByteBuffer>) property(PropertyKey.FILE_KEY_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    default Supplier<ByteBuffer> fileKeySupplier(
//        final Supplier<ByteBuffer> keyBufferSupplier) {
//
//        return (Supplier<ByteBuffer>) property(PropertyKey.FILE_KEY_SUPPLIER,
//                                               keyBufferSupplier)
//            .orElse(null);
//    }
    @SuppressWarnings("unchecked")
    default Supplier<FileOperation> fileOperationSupplier() {

        return (Supplier<FileOperation>) property(
            PropertyKey.FILE_OPERATION_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<FileOperation> fileOperationSupplier(
        final Supplier<FileOperation> fileOperationSupplier) {

        return (Supplier<FileOperation>) property(
            PropertyKey.FILE_OPERATION_SUPPLIER, fileOperationSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer() {

        return (Consumer<String>) property(PropertyKey.PATH_NAME_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer(
        final Consumer<String> pathNameConsumer) {

        return (Consumer<String>) property(PropertyKey.PATH_NAME_CONSUMER,
                                           pathNameConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> pathNameSupplier() {

        return (Supplier<String>) property(PropertyKey.PATH_NAME_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> pathNameSupplier(
        final Supplier<String> pathNameSupplier) {

        return (Supplier<String>) property(PropertyKey.PATH_NAME_SUPPLIER,
                                           pathNameSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer() {

        return (Consumer<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer(
        final Consumer<ReadableByteChannel> sourceChannelConsumer) {

        return (Consumer<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_CONSUMER, sourceChannelConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier() {

        return (Supplier<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier(
        final Supplier<ReadableByteChannel> sourceChannelSuppleir) {

        return (Supplier<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_SUPPLIER, sourceChannelSuppleir)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer() {

        return (Consumer<Long>) property(PropertyKey.SOURCE_COPIED_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer(
        final Consumer<Long> sourceCopiedConsumer) {

        return (Consumer<Long>) property(PropertyKey.SOURCE_COPIED_CONSUMER,
                                         sourceCopiedConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeySupplier() {

        return (Supplier<ByteBuffer>) property(PropertyKey.SOURCE_KEY_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeySupplier(
        final Supplier<ByteBuffer> sourceKeySupplier) {

        return (Supplier<ByteBuffer>) property(PropertyKey.SOURCE_KEY_SUPPLIER,
                                               sourceKeySupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> sourceObjectConsumer() {

        return (Consumer<Object>) property(PropertyKey.SOURCE_OBJECT_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> sourceObjectConsumer(
        final Consumer<Object> sourceObjectConsumer) {

        return (Consumer<Object>) property(PropertyKey.SOURCE_OBJECT_CONSUMER,
                                           sourceObjectConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> targetCopiedConsumer() {

        return (Consumer<Long>) property(PropertyKey.TARGET_COPIED_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> targetCopiedConsumer(
        final Consumer<Long> targetCopiedConsumer) {

        return (Consumer<Long>) property(PropertyKey.TARGET_COPIED_CONSUMER,
                                         targetCopiedConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeySupplier() {

        return (Supplier<ByteBuffer>) property(PropertyKey.TARGET_KEY_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeySupplier(
        final Supplier<ByteBuffer> targetKeySupplier) {

        return (Supplier<ByteBuffer>) property(PropertyKey.TARGET_KEY_SUPPLIER,
                                               targetKeySupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<WritableByteChannel> targetChannelConsumer() {

        return (Consumer<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<WritableByteChannel> targetChannelConsumer(
        final Consumer<WritableByteChannel> targetChannelConsumer) {

        return (Consumer<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_CONSUMER, targetChannelConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier() {

        return (Supplier<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier(
        final Supplier<WritableByteChannel> targetChannelSupplier) {

        return (Supplier<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_SUPPLIER, targetChannelSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> targetObjectConsumer() {

        return (Consumer<Object>) property(PropertyKey.TARGET_OBJECT_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> targetObjectConsumer(
        final Consumer<Object> targetObjectConsumer) {

        return (Consumer<Object>) property(PropertyKey.TARGET_OBJECT_CONSUMER,
                                           targetObjectConsumer)
            .orElse(null);
    }


}

