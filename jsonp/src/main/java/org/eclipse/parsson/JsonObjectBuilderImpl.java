/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.eclipse.parsson;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.parsson.api.BufferPool;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonConfig.KeyStrategy;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

/**
 * JsonObjectBuilder implementation
 *
 * @author Jitendra Kotamraju
 * @author Kin-man Chung
 */
class JsonObjectBuilderImpl implements JsonObjectBuilder {

    protected Map<String, JsonValue> valueMap;
    private final BufferPool bufferPool;
    private final DuplicateStrategy duplicateStrategy;

    JsonObjectBuilderImpl(BufferPool bufferPool) {
        this(bufferPool, false, Collections.emptyMap());
    }
    
    JsonObjectBuilderImpl(BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
        this.bufferPool = bufferPool;
        this.duplicateStrategy = DuplicateStrategy.strategyFromProperty(config.get(jakarta.json.JsonConfig.KEY_STRATEGY), rejectDuplicateKeys);
    }

    JsonObjectBuilderImpl(JsonObject object, BufferPool bufferPool) {
        this(object, bufferPool, false, Collections.emptyMap());
    }
    
    JsonObjectBuilderImpl(JsonObject object, BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
        this.bufferPool = bufferPool;
        valueMap = new LinkedHashMap<>();
        valueMap.putAll(object);
        this.duplicateStrategy = DuplicateStrategy.strategyFromProperty(config.get(jakarta.json.JsonConfig.KEY_STRATEGY), rejectDuplicateKeys);
    }

    JsonObjectBuilderImpl(Map<String, ?> map, BufferPool bufferPool) {
        this(map, bufferPool, false, Collections.emptyMap());
    }
    
    JsonObjectBuilderImpl(Map<String, ?> map, BufferPool bufferPool, boolean rejectDuplicateKeys, Map<String, ?> config) {
    	this.bufferPool = bufferPool;
    	valueMap = new LinkedHashMap<>();
    	populate(map);
    	this.duplicateStrategy = DuplicateStrategy.strategyFromProperty(config.get(jakarta.json.JsonConfig.KEY_STRATEGY), rejectDuplicateKeys);
    }

    @Override
    public JsonObjectBuilder add(String name, JsonValue value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, String value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, new JsonStringImpl(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigInteger value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigDecimal value) {
        validateName(name);
        validateValue(value);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, int value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, long value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, double value) {
        validateName(name);
        putValueMap(name, JsonNumberImpl.getJsonNumber(value));
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, boolean value) {
        validateName(name);
        putValueMap(name, value ? JsonValue.TRUE : JsonValue.FALSE);
        return this;
    }

    @Override
    public JsonObjectBuilder addNull(String name) {
        validateName(name);
        putValueMap(name, JsonValue.NULL);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
        validateName(name);
        if (builder == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_OBJECT_BUILDER_NULL());
        }
        putValueMap(name, builder.build());
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
        validateName(name);
        if (builder == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_ARRAY_BUILDER_NULL());
        }
        putValueMap(name, builder.build());
        return this;
    }

    @Override
    public JsonObjectBuilder addAll(JsonObjectBuilder builder) {
        if (builder == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_OBJECT_BUILDER_NULL());
        }
        if (valueMap == null) {
            this.valueMap = new LinkedHashMap<>();
        }
        this.valueMap.putAll(builder.build());
        return this;
    }

    @Override
    public JsonObjectBuilder remove(String name) {
        validateName(name);
        this.valueMap.remove(name);
        return this;
    }

    @Override
    public JsonObject build() {
        Map<String, JsonValue> snapshot = (valueMap == null)
                ? Collections.<String, JsonValue>emptyMap()
                : Collections.unmodifiableMap(valueMap);
        valueMap = null;
        return new JsonObjectImpl(snapshot, bufferPool);
    }

    private void populate(Map<String, ?> map) {
        final Set<String> fields = map.keySet();
        for (String field : fields) {
            Object value = map.get(field);
            if (value != null && value instanceof Optional) {
                ((Optional<?>) value).ifPresent(v ->
                        this.valueMap.put(field, MapUtil.handle(v, bufferPool)));
            } else {
                this.valueMap.put(field, MapUtil.handle(value, bufferPool));
            }
        }
    }

    private void putValueMap(String name, JsonValue value) {
        if (valueMap == null) {
            this.valueMap = new LinkedHashMap<>();
        }
        JsonValue previousValue = valueMap.get(name);
        valueMap.put(name, duplicateStrategy.getValue(name, value, previousValue));
    }

    private void validateName(String name) {
        if (name == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_NAME_NULL());
        }
    }

    private void validateValue(Object value) {
        if (value == null) {
            throw new NullPointerException(JsonMessages.OBJBUILDER_VALUE_NULL());
        }
    }

    private static final class JsonObjectImpl extends AbstractMap<String, JsonValue> implements JsonObject {
        private final Map<String, JsonValue> valueMap;      // unmodifiable
        private final BufferPool bufferPool;
        private int hashCode;

        JsonObjectImpl(Map<String, JsonValue> valueMap, BufferPool bufferPool) {
            this.valueMap = valueMap;
            this.bufferPool = bufferPool;
        }

        @Override
        public JsonArray getJsonArray(String name) {
            return (JsonArray)get(name);
        }

        @Override
        public JsonObject getJsonObject(String name) {
            return (JsonObject)get(name);
        }

        @Override
        public JsonNumber getJsonNumber(String name) {
            return (JsonNumber)get(name);
        }

        @Override
        public JsonString getJsonString(String name) {
            return (JsonString)get(name);
        }

        @Override
        public String getString(String name) {
            return getJsonString(name).getString();
        }

        @Override
        public String getString(String name, String defaultValue) {
            JsonValue value = get(name);
            if (value instanceof JsonString) {
                return ((JsonString) value).getString();
            } else {
                return defaultValue;
            }
        }

        @Override
        public int getInt(String name) {
            return getJsonNumber(name).intValue();
        }

        @Override
        public int getInt(String name, int defaultValue) {
            JsonValue value = get(name);
            if (value instanceof JsonNumber) {
                return ((JsonNumber) value).intValue();
            } else {
                return defaultValue;
            }
        }

        @Override
        public boolean getBoolean(String name) {
            JsonValue value = get(name);
            if (value == null) {
                throw new NullPointerException();
            } else if (value == JsonValue.TRUE) {
                return true;
            } else if (value == JsonValue.FALSE) {
                return false;
            } else {
                throw new ClassCastException();
            }
        }

        @Override
        public boolean getBoolean(String name, boolean defaultValue) {
            JsonValue value = get(name);
            if (value == JsonValue.TRUE) {
                return true;
            } else if (value == JsonValue.FALSE) {
                return false;
            } else {
                return defaultValue;
            }
        }

        @Override
        public boolean isNull(String name) {
            return get(name).equals(JsonValue.NULL);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.OBJECT;
        }

        @Override
        public Set<Entry<String, JsonValue>> entrySet() {
            return valueMap.entrySet();
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = super.hashCode();
            }
            return hashCode;
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            try (JsonWriter jw = new JsonWriterImpl(sw, bufferPool)) {
                jw.write(this);
            }
            return sw.toString();
        }

        @Override
        public JsonObject asJsonObject() {
            return this;
        }

        @Override
        public int size() {
            return valueMap.size();
        }

        @Override
        public JsonValue get(Object key) {
            return valueMap.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return valueMap.containsKey(key);
        }
    }

    private static enum DuplicateStrategy {

        NONE(KeyStrategy.NONE) {
            @Override
            protected JsonValue getValue(String name, JsonValue value, JsonValue previous) {
                if (previous != null) {
                    throw new IllegalStateException(JsonMessages.DUPLICATE_KEY(name));
                } else {
                    return value;
                }
            }
        },
        FIRST(KeyStrategy.FIRST) {
            @Override
            protected JsonValue getValue(String name, JsonValue value, JsonValue previous) {
                if (previous != null) {
                    return previous;
                } else {
                    return value;
                }
            }
        },
        LAST(KeyStrategy.LAST) {
            @Override
            protected JsonValue getValue(String name, JsonValue value, JsonValue previous) {
                return value;
            }
        };

        private final KeyStrategy property;

        private DuplicateStrategy(KeyStrategy property) {
            this.property = property;
        }
        
        private static DuplicateStrategy strategyFromProperty(Object value, boolean rejectDuplicateKeys) {
            if (value != null) {
                for (DuplicateStrategy strategy : DuplicateStrategy.values()) {
                    if (strategy.property.equals(value)) {
                        return strategy;
                    }
                }
            }
            if (rejectDuplicateKeys) {
                return DuplicateStrategy.NONE;
            } else {
                return DuplicateStrategy.LAST;
            }
        }
        
        protected abstract JsonValue getValue(String name, JsonValue value, JsonValue previous);
    }
}
