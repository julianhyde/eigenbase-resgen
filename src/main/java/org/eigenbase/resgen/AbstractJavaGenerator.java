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

import java.io.PrintWriter;
import java.io.File;

/**
 * Abstract base for all generators which generate Java code.
 *
 * @author jhyde
 */
abstract class AbstractJavaGenerator extends AbstractGenerator
{
    protected final String className;
    protected final ResourceDef.ResourceBundle resourceBundle;
    protected final String baseClassName;

    private static final String JAVA_STRING = "String";
    private static final String JAVA_NUMBER = "Number";
    private static final String JAVA_DATE_TIME = "java.util.Date";
    private static final String[] JAVA_TYPE_NAMES =
        {JAVA_STRING, JAVA_NUMBER, JAVA_DATE_TIME, JAVA_DATE_TIME};

    AbstractJavaGenerator(
        File srcFile,
        File file,
        String className,
        ResourceDef.ResourceBundle resourceBundle,
        String baseClassName)
    {
        super(srcFile, file);
        this.className = className;
        this.baseClassName = baseClassName;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Returns the type of error which is to be thrown by this resource.
     * Result is null if this is not an error.
     *
     * @param exception Exception element
     * @return Error class name
     */
    protected String getErrorClass(
            ResourceDef.Exception exception) {
        if (exception.className != null) {
            return exception.className;
        } else if (resourceBundle.exceptionClassName != null) {
            return resourceBundle.exceptionClassName;
        } else {
            return "java.lang.RuntimeException";
        }
    }

    protected String getPackageName()
    {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        } else {
            return className.substring(0,lastDot);
        }
    }

    protected String[] getArgTypes(String message) {
        return ResourceDefinition.getArgTypes(message, JAVA_TYPE_NAMES);
    }

    protected void generateHeader(PrintWriter pw) {
        generateDoNotModifyHeader(pw);
        String packageName = getPackageName();
        if (packageName != null) {
            pw.println("package " + packageName + ";");
        }
        pw.println("import java.io.IOException;");
        pw.println("import java.util.Locale;");
        pw.println("import java.util.ResourceBundle;");
        pw.println("import org.eigenbase.resgen.*;");
        pw.println();
        generateGeneratedByBlock(pw);
    }

    protected void generateFooter(PrintWriter pw, String className) {
        pw.println("// End " + className + ".java");
    }

    protected String getClassName()
    {
        return className;
    }

    protected String getBaseClassName()
    {
        return baseClassName;
    }
}

// End AbstractJavaGenerator.java
