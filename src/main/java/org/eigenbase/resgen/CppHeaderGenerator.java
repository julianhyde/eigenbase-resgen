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
import java.io.PrintWriter;

/**
 * Generates a C++ header file containing resource definitions.
 *
 * @author jhyde
 */
public class CppHeaderGenerator extends CppGenerator
{
    /**
     * Creates a C++ header generator.
     *
     * @param srcFile Source file
     * @param file File
     * @param className Class name
     * @param baseClassName Name of base class, must not be null, typically
     * @param defaultExceptionClassName Default exception class name
     */
    public CppHeaderGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        String defaultExceptionClassName)
    {
        super(srcFile, file, className, baseClassName,
            defaultExceptionClassName, null);
    }

    public void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList,
        PrintWriter pw)
    {
        generateDoNotModifyHeader(pw);
        generateGeneratedByBlock(pw);

        StringBuffer ifndef = new StringBuffer();
        String fileName = getFile().getName();
        ifndef.append(fileName.substring(0, fileName.length() - 2));
        ifndef.append("_Included");
        if (resourceList.cppNamespace != null) {
            ifndef.insert(0, '_');
            ifndef.insert(0, resourceList.cppNamespace.substring(1));
            ifndef.insert(0,
                Character.toUpperCase(resourceList.cppNamespace.charAt(0)));
        }

        pw.println("#ifndef " + ifndef.toString());
        pw.println("#define " + ifndef.toString());
        pw.println();
        pw.println("#include <ctime>");
        pw.println("#include <string>");
        pw.println();
        pw.println("#include \"Locale.h\"");
        pw.println("#include \"ResourceDefinition.h\"");
        pw.println("#include \"ResourceBundle.h\"");
        pw.println();

        pw.println("// begin includes specified by " + getSrcFileForComment());
        if (resourceList.cppExceptionClassLocation != null) {
            pw.println("#include \""
                       + resourceList.cppExceptionClassLocation
                       + "\"");
        }

        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            if (resource instanceof ResourceDef.Exception) {
                ResourceDef.Exception exception =
                    (ResourceDef.Exception)resource;

                if (exception.cppClassLocation != null) {
                    pw.println("#include \""
                               + exception.cppClassLocation
                               + "\"");
                }
            }
        }
        pw.println("// end includes specified by " + getSrcFileForComment());
        pw.println();
        if (resourceList.cppNamespace != null) {
            pw.println("namespace " + resourceList.cppNamespace + " {");
            pw.println();
        }

        pw.println();

        String baseClass = getBaseClassName();
        String className = getClassName();
        String bundleCacheClassName = className + "BundleCache";

        pw.println("class " + className + ";");
        pw.println("typedef map<Locale, " + className + "*> "
                   + bundleCacheClassName + ";");
        pw.println();
        pw.println("class " + className + " : " + baseClass);
        pw.println("{");
        pw.println("    protected:");
        pw.println("    explicit " + className + "(Locale locale);");
        pw.println();
        pw.println("    public:");
        pw.println("    virtual ~" + className + "() { }");
        pw.println();
        pw.println("    static const " + className + " &instance();");
        pw.println("    static const "
                   + className
                   + " &instance(const Locale &locale);");
        pw.println();

        pw.println("    static void setResourceFileLocation(const std::string &location);");
        pw.println();

        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            String text = resource.text.cdata;
            String comment = ResourceGen.getComment(resource);
            String parameterList = getParameterList(text);

            // e.g. "Internal"
            final String resourceInitCap =
                ResourceGen.getResourceInitcap(resource);

            Util.generateCommentBlock(pw, resource.name, text, comment);

            pw.println("    std::string " + resource.name + "("
                       + parameterList + ") const;");

            if (resource instanceof ResourceDef.Exception) {
                ResourceDef.Exception exception =
                    (ResourceDef.Exception)resource;

                String exceptionClass = exception.cppClassName;
                if (exceptionClass == null) {
                    exceptionClass = resourceList.cppExceptionClassName;
                }

                pw.println("    " + exceptionClass
                           + "* new" + resourceInitCap + "("
                           + parameterList + ") const;");

                boolean chainExceptions =
                    (exception.cppChainExceptions != null &&
                     exception.cppChainExceptions.equalsIgnoreCase("true"));

                if (chainExceptions) {
                    if (parameterList.length() > 0) {
                        pw.println("    "
                                   + exceptionClass
                                   + "* new"
                                   + resourceInitCap
                                   + "("
                                   + parameterList
                                   + ", const "
                                   + exceptionClass
                                   + " * const prev) const;");
                    } else {
                        pw.println("  "
                                   + exceptionClass
                                   + " new"
                                   + resourceInitCap + "("
                                   + "const "
                                   + exceptionClass
                                   + " * const prev) const;");
                    }
                }
            }

            pw.println();
        }

        pw.println("    private:");
        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            pw.println("    ResourceDefinition _" + resource.name + ";");
        }
        pw.println();

        pw.println("    template<class _GRB, class _BC, class _BC_ITER>");
        pw.println("        friend _GRB *makeInstance(_BC &bundleCache, const Locale &locale);");

        pw.println("};");


        if (resourceList.cppNamespace != null) {
            pw.println();
            pw.println("} // end namespace " + resourceList.cppNamespace);
        }

        pw.println();
        pw.println("#endif // " + ifndef.toString());
    }

}

// End CppHeaderGenerator.java
