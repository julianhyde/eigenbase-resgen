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

import java.util.Locale;

/**
 * A <code>Resource</code> is a collection of messages for a particular
 * software component and locale. It is loaded from an XML file whose root
 * element is <code>&lt;BaflResourceList&gt;</code>.
 *
 * <p>Given such an XML file, {@link ResourceGen} can generate Java a wrapper
 * class which implements this interface, and also has a method to create an
 * error for each message.</p>
 *
 * @author jhyde
 */
public interface Resource {
    /**
     * Populates this <code>Resource</code> from a URL.
     *
     * @param url The URL of the XML file containing the error messages
     * @param locale The ISO locale code (e.g. <code>"en"</code>, or
     *    <code>"en_US"</code>, or <code>"en_US_WIN"</code>) of the messages
     * @throws java.io.IOException if <code>url</code> cannot be opened, or if
     *    the format of its contents are invalid
     */
    void init(java.net.URL url, Locale locale) throws java.io.IOException;

    /**
     * Populates this <code>Resource</code> from an XML document.
     *
     * @param resourceList The URL of the XML file containing the error messages
     * @param locale The ISO locale code (e.g. <code>"en"</code>, or
     *    <code>"en_US"</code>, or <code>"en_US_WIN"</code>) of the messages
     */
    void init(ResourceDef.ResourceBundle resourceList, Locale locale);

    /**
     * Returns the locale of the messages.
     *
     * @return Locale of the messages
     */
    Locale getLocale();

    /**
     * Formats the message corresponding to <code>code</code> with the given
     * arguments. If an argument is not supplied, the tokens remain in the
     * returned message string.
     *
     * @param code Code
     * @param args Arguments
     * @return Formatted message
     */
    String formatError(int code, Object[] args);

    /**
     * Returns the severity of this message.
     *
     * @param code Code
     * @return Severity of the message
     */
    int getSeverity(int code);

    int SEVERITY_INFO = 0;
    int SEVERITY_ERR  = 1;
    int SEVERITY_WARN = 2;
    int SEVERITY_NON_FATAL_ERR = 3;
}

// End Resource.java
