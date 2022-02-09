/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package jakarta.json.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

/**
 * Service provider for JSON processing objects.
 *
 * <p>All the methods in this class are safe for use by multiple concurrent
 * threads.
 *
 * @see ServiceLoader
 */
public abstract class JsonProvider {

    /**
     * The name of the property that contains the name of the class capable of creating new JsonProvider objects.
     */
    private static final String JSONP_PROVIDER_FACTORY = "jakarta.json.provider";

    /**
     * A constant representing the name of the default
     * {@code JsonProvider} implementation class.
     */
    private static final String DEFAULT_PROVIDER
            = "org.eclipse.jsonp.JsonProviderImpl";

    /**
     * Default constructor.
     */
    protected JsonProvider() {
    }

    /**
     * Creates a JSON provider object.
     *
     * Implementation discovery consists of following steps:
     * <ol>
     * <li>If the system property {@value #JSONP_PROVIDER_FACTORY} exists,
     *    then its value is assumed to be the provider factory class.
     *    This phase of the look up enables per-JVM override of the JsonProvider implementation.</li>
     * <li>The provider is loaded using the {@link ServiceLoader#load(Class)} method.</li>
     * <li>If all the steps above fail, then the rest of the look up is unspecified. That said,
     *    the recommended behavior is to simply look for some hard-coded platform default Jakarta
     *    JSON Processing implementation. This phase of the look up is so that a platform can have
     *    its own Jakarta JSON Processing implementation as the last resort.</li>
     * </ol>
     * Users are recommended to cache the result of this method.
     *
     * @see ServiceLoader
     * @return a JSON provider
     */
    public static JsonProvider provider() {
        if (LazyFactoryLoader.JSON_PROVIDER != null) {
            return newInstance(LazyFactoryLoader.JSON_PROVIDER);
        }
        ServiceLoader<JsonProvider> loader = ServiceLoader.load(JsonProvider.class);
        Iterator<JsonProvider> it = loader.iterator();
        if (it.hasNext()) {
            return it.next();
        }

        // handling OSGi (specific default)
        if (isOsgi()) {
            JsonProvider result = lookupUsingOSGiServiceLoader(JsonProvider.class);
            if (result != null) {
                return result;
            }
        }

        try {
            Class<?> clazz = Class.forName(DEFAULT_PROVIDER);
            return (JsonProvider) clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException x) {
            throw new JsonException(
                    "Provider " + DEFAULT_PROVIDER + " not found", x);
        } catch (Exception x) {
            throw new JsonException(
                    "Provider " + DEFAULT_PROVIDER + " could not be instantiated: " + x,
                    x);
        }
    }

    /**
     * Creates a new instance from the specified class
     * @param clazz class to instance
     * @return the JsonProvider instance
     * @throws IllegalArgumentException for reflection issues
     */
    private static JsonProvider newInstance(Class<? extends JsonProvider> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to create " + clazz.getName(), e);
        }
    }


    /**
     * Return the JSON name.
     * @param key The name
     * @return The JSON name.
     */
    public abstract JsonGenerator.Key createGeneratorKey(String key);

    /**
     * Creates a JSON parser from a character stream.
     *
     * @param reader i/o reader from which JSON is to be read
     * @return a JSON parser
     */
    public abstract JsonParser createParser(Reader reader);

    /**
     * Creates a JSON parser from the specified byte stream.
     * The character encoding of the stream is determined
     * as defined in <a href="http://tools.ietf.org/rfc/rfc7159.txt">RFC 7159
     * </a>.
     *
     * @param in i/o stream from which JSON is to be read
     * @throws JsonException if encoding cannot be determined
     *         or i/o error (IOException would be cause of JsonException)
     * @return a JSON parser
     */
    public abstract JsonParser createParser(InputStream in);

    /**
     * Creates a parser factory for creating {@link JsonParser} instances.
     *
     * @return a JSON parser factory
     *
    public abstract JsonParserFactory createParserFactory();
     */

    /**
     * Creates a parser factory for creating {@link JsonParser} instances.
     * The factory is configured with the specified map of
     * provider specific configuration properties. Provider implementations
     * should ignore any unsupported configuration properties specified in
     * the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON parsers. The map may be empty or null
     * @return a JSON parser factory
     */
    public abstract JsonParserFactory createParserFactory(Map<String, ?> config);

    /**
     * Creates a JSON generator for writing JSON text to a character stream.
     *
     * @param writer a i/o writer to which JSON is written
     * @return a JSON generator
     */
    public abstract JsonGenerator createGenerator(Writer writer);

    /**
     * Creates a JSON generator for writing JSON text to a byte stream.
     *
     * @param out i/o stream to which JSON is written
     * @return a JSON generator
     */
    public abstract JsonGenerator createGenerator(OutputStream out);

    /**
     * Creates a generator factory for creating {@link JsonGenerator} instances.
     *
     * @return a JSON generator factory
     *
    public abstract JsonGeneratorFactory createGeneratorFactory();
     */

    /**
     * Creates a generator factory for creating {@link JsonGenerator} instances.
     * The factory is configured with the specified map of provider specific
     * configuration properties. Provider implementations should
     * ignore any unsupported configuration properties specified in the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON generators. The map may be empty or null
     * @return a JSON generator factory
     */
    public abstract JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config);

    /** OSGI aware service loader by HK2 */
    private static final String OSGI_SERVICE_LOADER_CLASS_NAME = "org.glassfish.hk2.osgiresourcelocator.ServiceLoader";

    /**
     * Check availability of HK2 service loader.
     *
     * @return true if HK2 service locator is available
     */
    private static boolean isOsgi() {
        try {
            Class.forName(OSGI_SERVICE_LOADER_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Lookup the service class by the HK2 service locator.
     *
     * @param serviceClass service class
     * @param <T> type of the service
     * @return a provider
     */
    private static <T> T lookupUsingOSGiServiceLoader(Class<? extends T> serviceClass) {
        try {
            // Use reflection to avoid having any dependendcy on HK2 ServiceLoader class
            Class<?>[] args = new Class<?>[]{serviceClass};
            Class<?> target = Class.forName(OSGI_SERVICE_LOADER_CLASS_NAME);
            Method m = target.getMethod("lookupProviderInstances", Class.class);
            @SuppressWarnings({"unchecked"})
            Iterator<? extends T> iter = ((Iterable<? extends T>) m.invoke(null, (Object[]) args)).iterator();
            return iter.hasNext() ? iter.next() : null;
        } catch (Exception ignored) {
            // log and continue
            return null;
        }
    }

    /**
     * Lazy loads the class specified in System property with the key JSONP_PROVIDER_FACTORY.
     * If no property is set, the value of {@link #JSON_PROVIDER} will be null.
     * In case of errors an IllegalStateException is thrown.
     *
     */
    @SuppressWarnings("unchecked")
    private static class LazyFactoryLoader {

        /**
         * JSON provider class
         */
        private static final Class<? extends JsonProvider> JSON_PROVIDER;

        static {
            String className = System.getProperty(JSONP_PROVIDER_FACTORY);
            if (className != null) {
                try {
                    JSON_PROVIDER = (Class<? extends JsonProvider>) Class.forName(className);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Unable to create " + className, e);
                }
            } else {
                JSON_PROVIDER = null;
            }
        }
    }
}
