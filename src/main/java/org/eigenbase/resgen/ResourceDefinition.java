/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.resgen;

import java.text.MessageFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.util.ResourceBundle;
import java.util.Properties;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Definition of a resource such as a parameterized message or exception.
 *
 * <p>A resource is identified within a {@link ResourceBundle} by a text
 * <em>key</em>, and has a <em>message</em> in its base locale (which is
 * usually US-English (en_US)). It may also have a set of properties, which are
 * represented as name-value pairs.
 *
 * <p>A resource definition is immutable.
 *
 * @author jhyde
 */
public class ResourceDefinition
{
    public final String key;
    public final String baseMessage;
    private final String[] props;

    private static final String[] EmptyStringArray = new String[0];

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_TIME = 3;
    private static final String[] TypeNames =
        {"string", "number", "date", "time"};

    /**
     * Creates a resource definition with no properties.
     *
     * @param key Unique name for this resource definition.
     * @param baseMessage Message for this resource definition in the base
     *    locale.
     */
    public ResourceDefinition(String key, String baseMessage)
    {
        this(key, baseMessage, null);
    }

    /**
     * Creates a resource definition.
     *
     * @param key Unique name for this resource definition.
     * @param baseMessage Message for this resource definition in the base
     *    locale.
     * @param props Array of property name/value pairs.
     *    <code>null</code> means the same as an empty array.
     */
    public ResourceDefinition(String key, String baseMessage, String[] props)
    {
        this.key = key;
        this.baseMessage = baseMessage;
        if (props == null) {
            props = EmptyStringArray;
        }
        assert props.length % 2 == 0 :
            "Must have even number of property names/values";
        this.props = props;
    }

    /**
     * Returns this resource definition's key.
     *
     * @return Key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns this resource definition's message in the base locale.
     * (To find the message in another locale, you will need to load a
     * resource bundle for that locale.)
     *
     * @return Base message
     */
    public String getBaseMessage()
    {
        return baseMessage;
    }

    /**
     * Returns the properties of this resource definition.
     *
     * @return Properties
     */
    public Properties getProperties()
    {
        final Properties properties = new Properties();
        for (int i = 0; i < props.length; i++) {
            String prop = props[i];
            String value = props[++i];
            properties.setProperty(prop, value);
        }
        return properties;
    }

    /**
     * Returns the types of arguments.
     *
     * @return Argument types
     */
    public String[] getArgTypes()
    {
        return getArgTypes(baseMessage, TypeNames);
    }

    /**
     * Creates an instance of this definition with a set of parameters.
     * This is a factory method, which may be overridden by a derived class.
     *
     * @param bundle Resource bundle the resource instance will belong to
     *   (This contains the locale, among other things.)
     * @param args Arguments to populate the message's parameters.
     *   The arguments must be consistent in number and type with the results
     *   of {@link #getArgTypes}.
     * @return Resource instance
     */
    public ResourceInstance instantiate(ResourceBundle bundle, Object[] args)
    {
        return new Instance(bundle, this, args);
    }

    /**
     * Parses a message for the arguments inside it, and
     * returns an array with the types of those arguments.
     *
     * <p>For example, <code>getArgTypes("I bought {0,number} {2}s",
     * new String[] {"string", "number", "date", "time"})</code>
     * yields {"number", null, "string"}.
     * Note the null corresponding to missing message #1.
     *
     * @param message Message to be parsed.
     * @param typeNames Strings to return for types.
     * @return Array of type names
     */
    protected static String[] getArgTypes(String message, String[] typeNames)
    {
        assert typeNames.length == 4;
        Format[] argFormats;
        try {
            // We'd like to do
            //  argFormats = format.getFormatsByArgumentIndex()
            // but it doesn't exist until JDK 1.4, and we'd like this code
            // to work earlier.
            Method method = MessageFormat.class.getMethod(
                "getFormatsByArgumentIndex", (Class[]) null);
            try {
                MessageFormat format = new MessageFormat(message);
                argFormats = (Format[]) method.invoke(format, (Object[]) null);
                String[] argTypes = new String[argFormats.length];
                for (int i = 0; i < argFormats.length; i++) {
                    int x = formatToType(argFormats[i]);
                    argTypes[i] =  typeNames[x];
                }
                return argTypes;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.toString());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.toString());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.toString());
            }
        } catch (NoSuchMethodException e) {
            // Fallback pre JDK 1.4
            return getArgTypesByHand(message, typeNames);
        } catch (SecurityException e) {
            throw new RuntimeException(e.toString());
        }
    }

    protected static String [] getArgTypesByHand(
        String message,
        String[] typeNames)
    {
        assert typeNames.length == 4;
        String[] argTypes = new String[10];
        int length = 0;
        for (int i = 0; i < 10; i++) {
            final int type = getArgType(i, message);
            if (type != TYPE_UNKNOWN) {
                length = i + 1;
                argTypes[i] = typeNames[type];
            }
        }
        // Created a truncated copy (but keep intervening nulls).
        String[] argTypes2 = new String[length];
        System.arraycopy(argTypes, 0, argTypes2, 0, length);
        return argTypes2;
    }

    /**
     * Returns the type of the <code>i</code>th argument inside a message,
     * or {@link #TYPE_UNKNOWN} if not found.
     *
     * @param i Ordinal of argument
     * @param message Message to parse
     * @return Type code ({@link #TYPE_STRING} etc.)
     */
    protected static int getArgType(int i, String message) {
        String arg = "{" + Integer.toString(i); // e.g. "{1"
        int index = message.lastIndexOf(arg);
        if (index < 0) {
            return TYPE_UNKNOWN;
        }
        index += arg.length();
        int end = message.length();
        while (index < end && message.charAt(index) == ' ') {
            index++;
        }
        if (index < end && message.charAt(index) == ',') {
            index++;
            while (index < end && message.charAt(index) == ' ') {
                index++;
            }
            if (index < end) {
                String sub = message.substring(index);
                if (sub.startsWith("number")) {
                    return TYPE_NUMBER;
                } else if (sub.startsWith("date")) {
                    return TYPE_DATE;
                } else if (sub.startsWith("time")) {
                    return TYPE_TIME;
                } else if (sub.startsWith("choice")) {
                    return TYPE_UNKNOWN;
                }
            }
        }
        return TYPE_STRING;
    }


    /**
     * Converts a {@link Format} to a type code ({@link #TYPE_STRING} etc.)
     */
    private static int formatToType(Format format) {
        if (format == null) {
            return TYPE_STRING;
        } else if (format instanceof NumberFormat) {
            return TYPE_NUMBER;
        } else if (format instanceof DateFormat) {
            // might be date or time, but assume it's date
            return TYPE_DATE;
        } else {
            return TYPE_STRING;
        }
    }

    /**
     * Default implementation of {@link ResourceInstance}.
     */
    private static class Instance implements ResourceInstance {
        ResourceDefinition definition;
        ResourceBundle bundle;
        Object[] args;

        public Instance(
            ResourceBundle bundle,
            ResourceDefinition definition,
            Object[] args)
        {
            this.definition = definition;
            this.bundle = bundle;
            this.args = args;
        }

        public String toString()
        {
            String message = bundle.getString(definition.key);
            MessageFormat format = new MessageFormat(message);
            format.setLocale(bundle.getLocale());
            String formattedMessage = format.format(args);
            return formattedMessage;
        }
    }
}

// End ResourceDefinition.java
