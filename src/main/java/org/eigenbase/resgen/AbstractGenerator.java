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
import java.util.Date;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract base for all generators.
 *
 * @author jhyde
 */
abstract class AbstractGenerator implements Generator
{
    private final File srcFile;
    private final File file;
    private Boolean scmSafeComments = null;

    public AbstractGenerator(File srcFile, File file)
    {
        this.srcFile = srcFile;
        this.file = file;
    }

    public void setScmSafeComments(boolean enabled)
    {
        if (scmSafeComments != null) {
            throw new AssertionError(
                "SCM safe comment style may only be configured once.");
        }

        scmSafeComments = enabled ? Boolean.TRUE : Boolean.FALSE;
    }

    protected boolean useScmSafeComments()
    {
        return scmSafeComments != null && scmSafeComments.booleanValue();
    }

    /**
     * Generates code for a particular resource.
     *
     * @param resource Resource
     * @param pw Writer
     */
    protected abstract void generateResource(
        ResourceDef.Resource resource,
        PrintWriter pw);

    protected void generateDoNotModifyHeader(PrintWriter pw) {
        if (useScmSafeComments()) {
            pw.println(
                "// This class is generated. Do NOT modify it manually.");
        } else {
            pw.println("// This class is generated. Do NOT modify it, or");
            pw.println("// add it to source control.");
        }
        pw.println();
    }

    protected void generateGeneratedByBlock(PrintWriter pw) {
        pw.println("/**");
        pw.println(" * This class was generated");
        pw.println(" * by " + ResourceGen.class);

        String file = getSrcFileForComment();
        pw.println(" * from " + file);
        if (!useScmSafeComments()) {
            pw.println(" * on " + new Date().toString() + ".");
        }
        pw.println(" * It contains a list of messages, and methods to");
        pw.println(" * retrieve and format those messages.");
        pw.println(" */");
        pw.println();
    }

    /**
     * Returns the generator's output file.  e.g., "BirthdayResource.java"
     *
     * @return Output file
     */
    protected File getFile()
    {
        return file;
    }

    /**
     * Returns the XML or .properties source file, in a manner suitable
     * for use in source code comments.  Path information is stripped if
     * SCM-safe comment style is enabled.
     *
     * @return source file
     * @see #setScmSafeComments(boolean)
     */
    protected String getSrcFileForComment()
    {
        String filename = srcFile.toString().replace('\\', '/');
        if (useScmSafeComments()) {
            int slashPos = filename.lastIndexOf('/');
            if (slashPos > 0) {
                filename = "..." + filename.substring(slashPos);
            }
        }

        return filename;
    }

    /**
     * Returns the fully-qualified name of the class being generated,
     * for example "happy.BirthdayResource_en_US".
     *
     * @return Name of class
     */
    protected abstract String getClassName();

    /**
     * Returns the fully-qualified name of the base class.
     *
     * @return Name of base class
     */
    protected abstract String getBaseClassName();

    /**
     * Returns a parameter list string, e.g. "String p0, int p1".
     *
     * @param message Message to parse
     * @return Parameter list as a string
     */
    protected String getParameterList(String message) {
        final String [] types = getArgTypes(message);
        if (types.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(type);

            // If this is a C++ pointer type, say "const char *", don't put
            // a space between it and the variable name.
            if (!type.endsWith("&") && !type.endsWith("*")) {
                sb.append(" ");
            }
            sb.append("p");
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }

    /**
     * Returns the number and types of parameters in the given error message,
     * expressed as an array of Strings (legal values are
     * currently "String", "Number", "java.util.Date", and null) ordered by
     * parameter number.
     *
     * @param message Message
     * @return Array of argument type names
     */
    protected abstract String [] getArgTypes(String message);

    protected String getArgumentList(String message)
    {
        final String [] types = getArgTypes(message);

        if (types.length == 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("p");
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }

}

// End AbstractGenerator.java
