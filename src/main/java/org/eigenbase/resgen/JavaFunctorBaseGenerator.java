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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a Java class for the base locale,
 * using the 'functor' code-generation style.
 *
 * <p>For each resource, the generated Java class contains one public, final,
 * non-static member. This member belongs to a class which has a number of
 * methods for creating strings or exceptions based upon this resource. The
 * methods are typesafe; that is, they have the same number and type of
 * parameters as the resource itself.
 *
 * @author jhyde
 */
public class JavaFunctorBaseGenerator extends JavaBaseGenerator
{
    private final Map functorMap = new HashMap();
    private final StringWriter functorSw = new StringWriter();
    private final PrintWriter functorPw = new PrintWriter(functorSw);

    JavaFunctorBaseGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        ResourceDef.ResourceBundle resourceBundle)
    {
        super(srcFile, file, className, baseClassName, resourceBundle);
    }

    public void generateResource(ResourceDef.Resource resource, PrintWriter pw) {
        if (resource.text == null) {
            throw new BuildException(
                    "Resource '" + resource.name + "' has no message");
        }
        String text = resource.text.cdata;
        String comment = ResourceGen.getComment(resource);
        final String resourceInitcap = ResourceGen.getResourceInitcap(resource);// e.g. "Internal"

        String parameterList = getParameterList(text);
        String argumentList = getArgumentList(text);
        String propList = getPropList(resource);
        String errorClassName;
        if (resource instanceof ResourceDef.Exception) {
            ResourceDef.Exception exception = (ResourceDef.Exception) resource;
            errorClassName = getErrorClass(exception);
        } else {
            errorClassName = null;
        }
        String functorType =
            getFunctorType(parameterList, argumentList, errorClassName);

        pw.println();
        Util.generateCommentBlock(pw, resource.name, text, comment);
        pw.println("    public final " + functorType + " " + resourceInitcap + " = new " + functorType + "(" + Util.quoteForJava(resourceInitcap) + ", " + Util.quoteForJava(text) + ", " + propList + ");");
    }

    private String getPropList(ResourceDef.Resource resource) {
        if (resource.properties == null || resource.properties.length == 0) {
            return "null";
        }
        final StringBuffer buf = new StringBuffer("new String[] {");
        for (int i = 0; i < resource.properties.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            ResourceDef.Property property = resource.properties[i];
            buf.append(Util.quoteForJava(property.name));
            buf.append(", ");
            buf.append(Util.quoteForJava(property.cdata));
        }
        buf.append("}");
        return buf.toString();
    }

    private String getFunctorType(
        String parameterList,
        String argumentList,
        String errorClassName)
    {
        List key = Arrays.asList(new String[] {parameterList, errorClassName});
        String functorType = (String) functorMap.get(key);
        if (functorType == null) {
            functorType = "_Def" + functorMap.size();
            functorMap.put(key, functorType);
            genFunctor(functorType, parameterList, argumentList, errorClassName, functorPw);
        }

        return functorType;
    }

    private void genFunctor(String functorType, String parameterList, String argumentList, String errorClassName, PrintWriter pw) {
        String definitionClass = "org.eigenbase.resgen.ResourceDefinition";
        final String classNameSansPackage = Util.removePackage(className);
        final String bundleThis = classNameSansPackage + ".this";
        String argumentArray = argumentList.equals("") ?
            "emptyObjectArray" :
            "new Object[] {" + argumentList + "}";
        pw.println();
        pw.println("    /**");
        pw.println("     * Definition for resources which");
        if (errorClassName != null) {
            pw.println("     * return a "
                + "{@link " + errorClassName + "} exception and");
        }
        pw.println("     * take arguments '" + parameterList + "'.");
        pw.println("     */");
        pw.println("    public final class " + functorType + " extends " + definitionClass + " {");
        pw.println("        " + functorType + "(String key, String baseMessage, String[] props) {");
        pw.println("            super(key, baseMessage, props);");
        pw.println("        }");
        pw.println("        public String str(" + parameterList + ") {");
        pw.println("            return instantiate(" + addLists(bundleThis, argumentArray) + ").toString();");
        pw.println("        }");
        if (errorClassName != null) {
            final ExceptionDescription ed = new ExceptionDescription(errorClassName);
            if (ed.hasInstCon()) {
                pw.println("        public " + errorClassName + " ex(" + parameterList + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + "));");
                pw.println("        }");
            } else if (ed.hasInstThrowCon()) {
                pw.println("        public " + errorClassName + " ex(" + parameterList + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + "), null);");
                pw.println("        }");
            } else if (ed.hasStringCon()) {
                pw.println("        public " + errorClassName + " ex(" + parameterList + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + ").toString());");
                pw.println("        }");
            } else if (ed.hasStringThrowCon()) {
                pw.println("        public " + errorClassName + " ex(" + parameterList + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + ").toString(), null);");
                pw.println("        }");
            }
            if (ed.hasInstThrowCon()) {
                pw.println("        public " + errorClassName + " ex(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + "), err);");
                pw.println("        }");
            } else if (ed.hasStringThrowCon()) {
                pw.println("        public " + errorClassName + " ex(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("            return new " + errorClassName + "(instantiate(" + addLists(bundleThis, argumentArray) + ").toString(), err);");
                pw.println("        }");
            }
        }
        pw.println("    }");
    }

    protected void postModule(PrintWriter pw) {
        functorPw.flush();
        pw.println(functorSw.toString());
    }
}

// End JavaFunctorBaseGenerator.java
