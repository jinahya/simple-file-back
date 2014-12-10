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


import static java.lang.invoke.MethodHandles.lookup;
import java.util.Arrays;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class FileBackTest {


    private static final Logger logger = getLogger(lookup().lookupClass());


    @Test(invocationCount = 1)
    public static void bytesToHex() {

        final byte[] bytes = new byte[current().nextInt(0, 128)];
        current().nextBytes(bytes);
        logger.debug("bytes: {}", Arrays.toString(bytes));

        final String actual = IntStream.range(0, bytes.length * 2)
            .map(i -> (bytes[i / 2] >> ((i & 1) == 0 ? 4 : 0)) & 0x0F)
            .mapToObj(Integer::toHexString)
            .collect(joining());
        logger.debug("actual: {}", actual);

        final String expected = Hex.encodeHexString(bytes);
        logger.debug("expected: {}", expected);

        assertEquals(actual, expected);
    }


}

