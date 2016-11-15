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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Ant task which processes a properties file and generates a C++ or Java class
 * from the resources in it.
 *
 * @author jhyde
 */
class PropertiesFileTask extends FileTask
{
    final Locale locale;

    PropertiesFileTask(ResourceGenTask.Include include, String fileName) {
        this.include = include;
        this.fileName = fileName;
        this.className = Util.fileNameToClassName(fileName, ".properties");
        this.locale = Util.fileNameToLocale(fileName, ".properties");
    }

    /**
     * Given an existing properties file such as
     * <code>happy/Birthday_fr_FR.properties</code>, generates the
     * corresponding Java class happy.Birthday_fr_FR.java</code>.
     *
     * <p>todo: Validate.
     */
    void process(ResourceGen generator) throws IOException
    {
        // e.g. happy/Birthday_fr_FR.properties
        String s = Util.fileNameSansLocale(fileName, ".properties");
        File file = new File(include.root.src, s + ".xml");
        URL url = Util.convertPathToURL(file);
        ResourceDef.ResourceBundle resourceList = Util.load(url);

        if (outputJava) {
            generateJava(generator, resourceList, locale);
        }
        if (outputCpp) {
            // We don't generate any C++ code from .properties file -- yet.
        }
    }
}

// End PropertiesFileTask.java
