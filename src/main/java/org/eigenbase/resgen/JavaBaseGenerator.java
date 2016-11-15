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

import org.apache.tools.ant.BuildException;

import java.io.PrintWriter;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a Java class for the base locale.
 *
 * @author jhyde
 */
class JavaBaseGenerator extends AbstractJavaGenerator
{
    protected final Set<String> warnedClasses = new HashSet<String>();

    JavaBaseGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        ResourceDef.ResourceBundle resourceBundle)
    {
        super(srcFile, file, className, resourceBundle, baseClassName);
    }

    public void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList, PrintWriter pw)
    {
        generateHeader(pw);
        String className = getClassName();
        final String classNameSansPackage = Util.removePackage(className);
        pw.print("public class " + classNameSansPackage);
        final String baseClass = getBaseClassName();
        if (baseClass != null) {
            pw.print(" extends " + baseClass);
        }
        pw.println(" {");
        pw.println("    public " + classNameSansPackage + "() throws IOException {");
        pw.println("    }");
        pw.println("    private static final String baseName = " + Util.quoteForJava(getClassName()) + ";");
        pw.println("    /**");
        pw.println("     * Retrieves the singleton instance of "
            + "{@link " + classNameSansPackage + "}. If");
        pw.println("     * the application has called {@link #setThreadLocale}, returns the");
        pw.println("     * resource for the thread's locale.");
        pw.println("     */");
        pw.println("    public static synchronized " + classNameSansPackage + " instance() {");
        pw.println("        return (" + classNameSansPackage + ") instance(baseName, getThreadOrDefaultLocale(), ResourceBundle.getBundle(baseName, getThreadOrDefaultLocale()));");
        pw.println("    }");
        pw.println("    /**");
        pw.println("     * Retrieves the instance of "
            + "{@link " + classNameSansPackage + "} for the given locale.");
        pw.println("     */");
        pw.println("    public static synchronized " + classNameSansPackage + " instance(Locale locale) {");
        pw.println("        return (" + classNameSansPackage + ") instance(baseName, locale, ResourceBundle.getBundle(baseName, locale));");
        pw.println("    }");
        if (resourceList.code != null) {
            pw.println("    // begin of included code");
            pw.print(resourceList.code.cdata);
            pw.println("    // end of included code");
        }

        for (int j = 0; j < resourceList.resources.length; j++) {
            ResourceDef.Resource resource = resourceList.resources[j];
            generateResource(resource, pw);
        }
        pw.println("");
        postModule(pw);
        pw.println("}");
    }

    protected void postModule(PrintWriter pw)
    {
    }

    public void generateResource(ResourceDef.Resource resource, PrintWriter pw)
    {
        if (resource.text == null) {
            throw new BuildException(
                    "Resource '" + resource.name + "' has no message");
        }
        String text = resource.text.cdata;
        String comment = ResourceGen.getComment(resource);
        final String resourceInitcap = ResourceGen.getResourceInitcap(resource);// e.g. "Internal"

        String definitionClass = "org.eigenbase.resgen.ResourceDefinition";
        String parameterList = getParameterList(text);
        String argumentList = getArgumentList(text); // e.g. "p0, p1"
        String argumentArray = argumentList.equals("") ?
            "emptyObjectArray" :
            "new Object[] {" + argumentList + "}"; // e.g. "new Object[] {p0, p1}"

        pw.println();
        Util.generateCommentBlock(pw, resource.name, text, comment);

        pw.println("    public static final " + definitionClass + " " + resourceInitcap + " = new " + definitionClass + "(\"" + resourceInitcap + "\", " + Util.quoteForJava(text) + ");");
        pw.println("    public String get" + resourceInitcap + "(" + parameterList + ") {");
        pw.println("        return " + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + ").toString();");
        pw.println("    }");
        if (resource instanceof ResourceDef.Exception) {
            ResourceDef.Exception exception = (ResourceDef.Exception) resource;
            String errorClassName = getErrorClass(exception);
            final ExceptionDescription ed = new ExceptionDescription(errorClassName);
            if (ed.hasInstCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "));");
                pw.println("    }");
            } else if (ed.hasInstThrowCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "), null);");
                pw.println("    }");
            } else if (ed.hasStringCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "));");
                pw.println("    }");
            } else if (ed.hasStringThrowCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "), null);");
                pw.println("    }");
            }
            if (ed.hasInstThrowCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "), err);");
                pw.println("    }");
            } else if (ed.hasStringThrowCon()) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "), err);");
                pw.println("    }");
            }
        }
    }

    /**
     * Description of the constructs that an exception class has.
     */
    class ExceptionDescription {
        final List<String> signatures = new ArrayList<String>();
        boolean hasInstCon;
        boolean hasInstThrowCon;
        boolean hasStringCon;
        boolean hasStringThrowCon;

        boolean hasInstCon() {
            return hasInstCon
                || signatures.contains("(ResourceInstance r)");
        }

        boolean hasInstThrowCon() {
            return hasInstThrowCon
                || signatures.contains("(ResourceInstance r, Throwable cause)");
        }

        boolean hasStringCon() {
            return hasStringCon
                || signatures.contains("(String message)");
        }

        boolean hasStringThrowCon() {
            return hasStringThrowCon
                || signatures.contains("(String message, Throwable cause)");
        }

        /**
         * Figures out what constructors the exception class has. We'd
         * prefer to use
         * <code>init(ResourceDefinition rd)</code> or
         * <code>init(ResourceDefinition rd, Throwable e)</code>
         * if it has them, but we can use
         * <code>init(String s)</code> and
         * <code>init(String s, Throwable e)</code>
         * as a fall-back.
         *
         * Prints a warming message if the class cannot be loaded.
         *
         * @param errorClassName Name of exception class
         */
        ExceptionDescription(String errorClassName)
        {
            hasInstCon = false;
            hasInstThrowCon = false;
            hasStringCon = false;
            hasStringThrowCon = false;
            try {
                Class errorClass;
                try {
                    errorClass = Class.forName(errorClassName);
                } catch (ClassNotFoundException e) {
                    // Might be in the java.lang package, for which we
                    // allow them to omit the package name.
                    errorClass = Class.forName("java.lang." + errorClassName);
                }
                Constructor[] constructors = errorClass.getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Constructor constructor = constructors[i];
                    Class[] types = constructor.getParameterTypes();
                    if (types.length == 1 &&
                        ResourceInstance.class.isAssignableFrom(types[0])) {
                        hasInstCon = true;
                    }
                    if (types.length == 1 &&
                        String.class.isAssignableFrom(types[0])) {
                        hasStringCon = true;
                    }
                    if (types.length == 2 &&
                        ResourceInstance.class.isAssignableFrom(types[0]) &&
                        Throwable.class.isAssignableFrom(types[1])) {
                        hasInstThrowCon = true;
                    }
                    if (types.length == 2 &&
                        String.class.isAssignableFrom(types[0]) &&
                        Throwable.class.isAssignableFrom(types[1])) {
                        hasStringThrowCon = true;
                    }
                }
            } catch (ClassNotFoundException e) {
                if (warnedClasses.add(errorClassName)) {
                    System.out.println("Warning: Could not find exception " +
                        "class '" + errorClassName + "' on classpath. " +
                        "Exception factory methods will not be generated.");
                }
                for (ResourceDef.Factory factory : resourceBundle.factories) {
                    if (factory.className.equals(errorClassName)) {
                        signatures.add(factory.signature);
                    }
                }
            }
        }
    }

    // helper
    protected static String addLists(String x, String y) {
        if (x == null || x.equals("")) {
            if (y == null || y.equals("")) {
                return "";
            } else {
                return y;
            }
        } else if (y == null || y.equals("")) {
            return x;
        } else {
            return x + ", " + y;
        }
    }

    protected static String addLists(String x, String y, String z) {
        return addLists(x, addLists(y, z));
    }
}

// End JavaBaseGenerator.java
