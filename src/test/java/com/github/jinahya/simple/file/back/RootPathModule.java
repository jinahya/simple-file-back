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


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.file.Path;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class RootPathModule extends AbstractModule {


    @Override
    protected void configure() {

        bind(Path.class)
            .annotatedWith(RootPath.class)
            .toInstance(FileBackTests.randomRootPath());
    }


    public <T> T inject(final Class<T> injecteeType) {

        final Injector injector = Guice.createInjector(this);

        return injector.getInstance(injecteeType);
    }


    public <T> T inject(final T injectee) {

        final Injector injector = Guice.createInjector(this);

        injector.injectMembers(injectee);

        return injectee;
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


}

