/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pegdown.ast;

import org.parboiled.common.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Attribute map for directives.
 */
public interface DirectiveAttributes {

    /**
     * Get all attribute keys.
     */
    public Set<String> keys();

    /**
     * Get the 'identifier' value (explicit or marked with '#').
     */
    public String identifier();

    /**
     * Get all 'class' values (explicit or marked with '.').
     */
    public List<String> classes();

    /**
     * Get the 'class' values as a single string, joined with spaces.
     */
    public String classesString();

    /**
     * Get the first value for a key.
     */
    public String value(String key);

    /**
     * Get the first value for a key, otherwise default.
     */
    public String value(String key, String defaultValue);

    /**
     * Get all values for a key.
     */
    public List<String> values(String key);

    /**
     * Get the first value for a key as an int.
     */
    public int intValue(String key, int defaultValue);

    /**
     * Get the first value for a key as a boolean.
     */
    public boolean booleanValue(String key, boolean defaultValue);

    /**
     * Are there no attributes defined?
     */
    public boolean isEmpty();

    /**
     * Default implementation of attributes, based on a hash map.
     */
    public static class AttributeMap implements DirectiveAttributes {

        private final HashMap<String, List<String>> map = new HashMap<String, List<String>>();

        public String add(String key, String value) {
            List<String> values = map.containsKey(key) ? map.get(key) : new ArrayList<String>();
            values.add(value);
            map.put(key, values);
            return value;
        }

        public Set<String> keys() {
            return map.keySet();
        }

        public String identifier() {
            return value("identifier");
        }

        public List<String> classes() {
            return values("class");
        }

        public String classesString() {
            return StringUtils.join(classes(), " ");
        }

        public String value(String key) {
            List<String> values = values(key);
            return values.isEmpty() ? null : values.get(0);
        }

        public String value(String key, String defaultValue) {
            String value = value(key);
            return value == null ? defaultValue : value;
        }

        public List<String> values(String key) {
            return map.containsKey(key) ? map.get(key) : Collections.<String>emptyList();
        }

        public int intValue(String key, int defaultValue) {
            String value = value(key);
            try {
                return value == null ? defaultValue : Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public boolean booleanValue(String key, boolean defaultValue) {
            String value = value(key);
            if (value == null)
                return defaultValue;
            else if ("true".equals(value) || "on".equals(value) || "yes".equals(value))
                return true;
            else if ("false".equals(value) || "off".equals(value) || "no".equals(value))
                return false;
            else
                return defaultValue;
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                sb.append(" " + key + "=" + value);
            }
            sb.append(" }");
            return sb.toString();
        }
    }

    /**
     * Var wrapper for attributes.
     */
    public static class Var extends org.parboiled.support.Var<AttributeMap> {

        public boolean add(String value, String key) {
            if (get() == null) set(new AttributeMap());
            get().add(key, value);
            return true;
        }

        public DirectiveAttributes getAttributes() {
            return get() != null ? get() : new AttributeMap();
        }
    }

}
