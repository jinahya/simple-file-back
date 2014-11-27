/*
 * Copyright 2014 Jin Kwon.
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;


/**
 *
 */
public final class FileBackUtilities {


    public static Stream<String> split(
        final byte[] keyBytes, final String digestAlgorithm,
        final Function<byte[], String> identifierFunction,
        final int tokenLength)
        throws NoSuchAlgorithmException {

        if (keyBytes == null) {
            throw new NullPointerException("null keyBytes");
        }

        if (keyBytes.length == 0) {
            throw new IllegalArgumentException(
                "keyBytes.length(" + keyBytes.length + ") == 0");
        }

        if (tokenLength < 0) {
            throw new IllegalArgumentException(
                "tokenLength(" + tokenLength + ") < 0");
        }

        final byte[] digested
            = MessageDigest.getInstance(digestAlgorithm).digest(keyBytes);
        final String identified = identifierFunction.apply(digested);

        return Stream.of(identified.split("(?<=\\G.{" + tokenLength + "})"));
    }


    /**
     *
     * @param keyBytes
     * @param digestAlgorithm
     * @param identifierFuction
     * @param tokenLength
     * @param tokenDelimiter
     *
     * @return A joined string.
     *
     * @throws NoSuchAlgorithmException
     * @see #split(byte[], java.lang.String, java.util.function.Function, int)
     */
    public static String join(
        final byte[] keyBytes, final String digestAlgorithm,
        final Function<byte[], String> identifierFuction,
        final int tokenLength, final CharSequence tokenDelimiter)
        throws NoSuchAlgorithmException {

        return split(keyBytes, digestAlgorithm, identifierFuction, tokenLength)
            .collect(joining(tokenDelimiter));
    }


    /**
     *
     * @param fileContext
     * @param propertyName
     *
     * @return
     *
     * @see FileContext#getProperty(java.lang.String)
     */
    public static Object getProperty(final FileContext fileContext,
                                     final String propertyName) {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Object value = fileContext.getProperty(propertyName);
        if (value == null) {
            throw new IllegalArgumentException(
                "no property for " + propertyName + " in " + fileContext);
        }

        return value;
    }


    public static <T> T getProperty(final FileContext fileContext,
                                    final String propertyName,
                                    final Class<T> propertyType) {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Object value = getProperty(fileContext, propertyName);
        assert value != null;

        if (!(propertyType.isInstance(value))) {
            throw new IllegalArgumentException(
                "no property of " + propertyType + " for " + propertyName
                + " in " + fileContext);
        }

        return propertyType.cast(value);
    }


    public static byte[] getKeyBytes(final FileContext fileContext) {

        return getProperty(fileContext, FileBackConstants.PROPERTY_KEY_BYTES,
                           byte[].class);
    }


    public static InputStream getSourceStream(final FileContext fileContext) {

        return getProperty(fileContext,
                           FileBackConstants.PROPERTY_SOURCE_STREAM,
                           InputStream.class);
    }


    public static OutputStream getTargetStream(final FileContext fileContext) {

        return getProperty(fileContext,
                           FileBackConstants.PROPERTY_TARGET_STREAM,
                           OutputStream.class);
    }


    private FileBackUtilities() {

        super();
    }


}

