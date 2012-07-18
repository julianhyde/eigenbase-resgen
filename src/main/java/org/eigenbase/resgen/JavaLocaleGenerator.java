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
import java.io.PrintWriter;
import java.io.File;

/**
 * Generates a Java class for a locale.
 *
 * @author jhyde
 */
public class JavaLocaleGenerator extends AbstractJavaGenerator
{
    private final Locale locale;

    JavaLocaleGenerator(
        File srcFile,
        File file,
        String className,
        ResourceDef.ResourceBundle resourceBundle,
        Locale locale,
        String baseClassName)
    {
        super(srcFile, file, className, resourceBundle, baseClassName);
        this.locale = locale;
    }

    public void generateModule(ResourceGen generator, ResourceDef.ResourceBundle resourceList, PrintWriter pw)
    {
        generateHeader(pw);
        // e.g. "happy.BirthdayResource_en_US"
        String className = getClassName();
        // e.g. "BirthdayResource_en_US"
        String classNameSansPackage = Util.removePackage(className);
        // e.g. "happy.BirthdayResource"
        final String baseClass = getBaseClassName();
        // e.g. "BirthdayResource"
        String baseClassSansPackage = Util.removePackage(baseClass);
        pw.println("public class " + classNameSansPackage + " extends " + baseClassSansPackage + " {");
        pw.println("    public " + classNameSansPackage + "() throws IOException {");
        pw.println("    }");
        pw.println("}");
        pw.println("");
        generateFooter(pw, classNameSansPackage);
    }

    public void generateResource(ResourceDef.Resource resource, PrintWriter pw)
    {
        throw new UnsupportedOperationException();
    }
}

// End JavaLocaleGenerator.java
