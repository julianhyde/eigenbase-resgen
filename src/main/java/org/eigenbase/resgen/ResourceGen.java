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

import org.eigenbase.xom.DOMWrapper;

import java.io.File;
import java.io.IOException;

/**
 * <code>ResourceGen</code> parses an XML file containing error messages, and
 * generates .java file to access the errors. Usage:<blockquote>
 *
 * <pre>ResourceGen xmlFile</pre>
 *
 * </blockquote>For example,<blockquote>
 *
 * <pre>java org.eigenbase.resgen.ResourceGen MyResource_en.xml</pre>
 *
 * </blockquote>
 *
 * <p>This will create class <code>MyResource</code>, with a
 * function corresponding to each error message in
 * <code>MyResource_en.xml</code>.</p>
 *
 * <p>See also the ANT Task, {@link ResourceGenTask}.</p>
 *
 * @author jhyde
 */
public class ResourceGen
{

    public static void main(String [] args) throws IOException
    {
        ResourceGenTask rootArgs = parse(args);
        new ResourceGen().run(rootArgs);
    }

    static ResourceGenTask parse(String[] args)
    {
        ResourceGenTask rootArgs = new ResourceGenTask();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-mode") && i + 1 < args.length) {
                rootArgs.setMode(args[++i]);
            } else if (arg.equals("-srcdir") && i + 1 < args.length) {
                rootArgs.setSrcdir(new File(args[++i]));
            } else if (arg.equals("-destdir") && i + 1 < args.length) {
                rootArgs.setDestdir(new File(args[++i]));
            } else if (arg.equals("-resdir") && i + 1 < args.length) {
                rootArgs.setResdir(new File(args[++i]));
            } else if (arg.equals("-locales") && i + 1 < args.length) {
                rootArgs.setLocales(args[++i]);
            } else if (arg.equals("-style") && i + 1 < args.length) {
                rootArgs.setStyle(args[++i]);
            } else if (arg.equals("-force")) {
                rootArgs.setForce(true);
            } else if (arg.equals("-commentstyle")) {
                rootArgs.setCommentStyle(args[++i]);
            } else {
                ResourceGenTask.Include resourceArgs =
                        new ResourceGenTask.Include();
                rootArgs.addInclude(resourceArgs);
                resourceArgs.setName(arg);
            }
        }
        if (rootArgs.getIncludes().length == 0) {
            throw new java.lang.Error("No input file specified.");
        }
        if (rootArgs.getDestdir() == null) {
            rootArgs.setDestdir(rootArgs.getSrcdir());
        }
        return rootArgs;
    }

    void run(ResourceGenTask rootArgs) throws IOException {
        rootArgs.validate();
        final ResourceGenTask.Include[] includes = rootArgs.getIncludes();
        for (int i = 0; i < includes.length; i++) {
            includes[i].process(this);
        }
    }

    /**
     * Prints a message to the output stream.
     */
    void comment(String message)
    {
        System.out.println(message);
    }

    /**
     * Returns the name of the resource with the first letter capitalized,
     * suitable for use in method names. For example, "MyErrorMessage".
     */
    static String getResourceInitcap(ResourceDef.Resource resource)
    {
        String name = resource.name;
        if (name.equals(name.toUpperCase())) {
            return "_" + name;
        } else {
            return name.substring(0,1).toUpperCase() + name.substring(1);
        }
    }

    /**
     * Returns any comment relating to the message.
     */
    static String getComment(ResourceDef.Resource resource)
    {
        DOMWrapper[] children = resource.getDef().getChildren();
        for (int i = 0; i < children.length; i++) {
            DOMWrapper child = children[i];
            if (child.getType() == DOMWrapper.COMMENT) {
                return child.getText(); // first comment only
            }
        }
        return null; // no comment
    }

    FileTask createXmlTask(
            ResourceGenTask.Include include, String fileName, String className,
            String baseClassName, boolean outputJava, String cppClassName,
            String cppBaseClassName, boolean outputCpp)
    {
        return new XmlFileTask(
            include, fileName, className, baseClassName,
            outputJava, cppClassName, cppBaseClassName,
            outputCpp);
    }

    FileTask createPropertiesTask(
            ResourceGenTask.Include include, String fileName) {
        return new PropertiesFileTask(include, fileName);
    }

}

// End ResourceGen.java
