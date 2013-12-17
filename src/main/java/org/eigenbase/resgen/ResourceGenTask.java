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
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A <code>ResourceGenTask</code> is an ANT task to invoke the Eigenbase
 * Resource Generator.
 *
 * <p>Example:<blockquote>
 *
 * <pre>&lt;resgen srcdir="source" locales="en_US"&gt;
 *    &lt;include name="happy/BirthdayResource.xml"/&gt;
 *&lt;/resgen&gt;</pre>
 *
 * </blockquote>generates<blockquote>
 *
 * <pre>source/happy/BirthdayResource.properties
 *source/happy/BirthdayResource_en_US.properties
 *source/happy/BirthdayResource.java
 *source/happy/BirthdayResource_en_US.java</pre>
 *
 * </blockquote>
 *
 * <p>C++ Example:<blockquote>
 *
 * <pre>&lt;resgen mode="c++" srcdir="source" locales="en_US"&gt;
 *    &lt;include name="happy/BirthdayResource.xml"/&gt;
 *&lt;/resgen&gt;</pre>
 *
 * </blockquote>generates<blockquote>
 *
 * <pre>source/happy/BirthdayResource.resources
 *source/happy/BirthdayResource_en_US.resources
 *source/happy/BirthdayResource.h
 *source/happy/BirthdayResource.cpp</pre></blockquote>
 *
 * <p>Files are not generated if there is an existing newer one.</p>
 *
 * <p>The output path is determined by 'destdir' (or 'resdir' for .properties
 * files) and the package-name (derived from the XML file's path relative to
 * 'srcdir'). Since the Java runtime environment searches for resource bundles
 * on the classpath, it is typical to set srcdir="src", destdir="src",
 * resdir="classes".</p>
 *
 * <h2>Element &lt;resourceGen&gt;</h2>
 *
 * <table border="2" summary="resourceGen attributes">
 * <tr>
 * <th>Attribute</th>
 * <th>Description</th>
 * <th>Required</th>
 * </tr>
 *
 * <tr>
 * <td><a name="mode">mode</a></td>
 * <td>Generation mode.  Acceptable values are "java", "c++" or "all".
 *     The default is "java".</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="srcdir">srcdir</a></td>
 * <td>Source directory. The paths of resource files, and hence the
 *     package names of generated Java classes, are relative to this
 *     directory.</td>
 * <td>Yes</td>
 * </tr>
 *
 * <tr>
 * <td><a name="destdir">destdir</a></td>
 * <td>Destination directory. Output .java files are generated relative to this
 *     directory. If not specified, has the same value as
 *     <a href="#srcdir">srcdir</a>.</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="resdir">resdir</a></td>
 * <td>Resource directory. Output .properties files are generated relative to
 *     this directory. If not specified, has the same value as
 *     <a href="#destdir">destdir</a>.</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="locales">locales</a></td>
 * <td>Comma-separated list of locales to generate files for.
 *     If not specified, uses the locale of the resource file.</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="style">style</a></td>
 * <td>Code-generation style. Values are "dynamic" or "functor".
 *     Default is "dynamic": generate several non-static methods for each
 *     resource.
 *     In the "functor" style, there is one member per resource, which has
 *     several methods.</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="force">force</a></td>
 * <td>Whether to generate files even if they do not appear to be out of
 *     date. Default is false.</td>
 * <td>No</td>
 * </tr>
 *
 * <tr>
 * <td><a name="commentstyle">commentstyle</a></td>
 * <td>Generated comment style.  Values are "normal" and "scm-safe".  The
 *     default is "normal": generates comments that indicate the source file's
 *     original path and states that the file should not be checked into source
 *     control systems.  The "scm-safe" comment style modifies the comments
 *     to make storage of the output files in an SCM more palatable.  It omits
 *     the source file's path and states that the file was generated and should
 *     not be edited manually.</td>
 * <td>No</td>
 * </table>
 *
 * Nested element: &lt;{@link Include include}&gt;.
 *
 * @author jhyde
 */
public class ResourceGenTask extends Task
{
    private ArrayList resources = new ArrayList();
    int mode = MODE_JAVA;
    File src;
    File dest;
    File res;
    int style = STYLE_DYNAMIC;
    String locales;
    boolean force;
    int commentStyle = COMMENT_STYLE_NORMAL;

    private static final int MODE_UNKNOWN = -1;
    private static final int MODE_JAVA = 1;
    private static final int MODE_CPP = 2;
    private static final int MODE_ALL = 3;

    public static final int STYLE_DYNAMIC = 1;
    public static final int STYLE_FUNCTOR = 2;

    public static final int COMMENT_STYLE_NORMAL = 1;
    public static final int COMMENT_STYLE_SCM_SAFE = 2;

    public ResourceGenTask()
    {
    }

    public void execute() throws BuildException
    {
        validate();
        try {
            new ResourceGen().run(this);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /** Called by ANT.
     *
     * @param resourceArgs Arguments
     */
    public void addInclude(Include resourceArgs)
    {
        resources.add(resourceArgs);
        resourceArgs.root = this;
    }

    void validate()
    {
        if (mode != MODE_JAVA && mode != MODE_CPP && mode != MODE_ALL) {
            throw new BuildException("You must specify a value mode: java, c++, or all");
        }

        if (src == null) {
            throw new BuildException("You must specify 'srcdir'");
        }
        if (dest == null) {
            dest = src;
        }
        if (res == null) {
            res = dest;
        }
        final Include[] args = getIncludes();
        for (int i = 0; i < args.length; i++) {
            args[i].validate();
        }
    }

    Include[] getIncludes()
    {
        return (Include[]) resources.toArray(new Include[0]);
    }

    /** Sets <a href="#mode">mode</a>.
     *
     * @param mode Mode
     */
    public void setMode(String mode)
        throws BuildException
    {
        if ("java".equals(mode)) {
            this.mode = MODE_JAVA;
        } else if ("c++".equals(mode)) {
            this.mode = MODE_CPP;
        } else if ("all".equals(mode)) {
            this.mode = MODE_ALL;
        } else {
            this.mode = MODE_UNKNOWN;
        }
    }

    /** Sets <a href="#srcdir">srcdir</a>.
     *
     * @param srcDir Source directory */
    public void setSrcdir(File srcDir)
    {
        this.src = srcDir;
    }

    /** Returns <a href="#srcdir">srcdir</a>.
     *
     * @return Source directory */
    public File getSrcdir()
    {
        return src;
    }

    /** Sets <a href="#destdir">destdir</a>.
     *
     * @param destDir Destination directory */
    public void setDestdir(File destDir)
    {
        this.dest = destDir;
    }

    /** Returns <a href="#destdir">destdir</a>.
     *
     * @return Destination directory */
    public File getDestdir()
    {
        return dest;
    }

    /** Sets <a href="#resdir">resdir</a>.
     *
     * @param resDir Resource directory */
    public void setResdir(File resDir)
    {
        this.res = resDir;
    }

    /** Sets <a href="#style">style</a>.
     *
     * @param style Style */
    public void setStyle(String style) throws BuildException
    {
        if (style.equals("dynamic")) {
            this.style = STYLE_DYNAMIC;
        } else if (style.equals("functor")) {
            this.style = STYLE_FUNCTOR;
        } else {
            throw new BuildException("Invalid style '" + style + "'");
        }
    }

    /** Sets <a href="#locales">locales</a>.
     *
     * @param locales Locales */
    public void setLocales(String locales) throws BuildException
    {
        this.locales = locales;
    }

    /** Sets <a href="#force">force</a>.
     *
     * @param force Force */
    public void setForce(boolean force)
    {
        this.force = force;
    }

    /** Sets <a href="#commentstyle">commentstyle</a>.
     *
     * @param commentStyle Comment style */
    public void setCommentStyle(String commentStyle) throws BuildException
    {
        if (commentStyle.equals("normal")) {
            this.commentStyle = COMMENT_STYLE_NORMAL;
        } else if (commentStyle.equals("scm-safe")) {
            this.commentStyle = COMMENT_STYLE_SCM_SAFE;
        } else {
            throw new BuildException(
                "Invalid commentstyle '" + commentStyle + "'");
        }
    }

    /**
     * <code>Include</code> implements &lt;include&gt; element nested
     * within a &lt;resgen&gt; task (see {@link ResourceGenTask}).
     *
     * <table border="2" summary="include attributes">
     * <tr>
     * <th>Attribute</th>
     * <th>Description</th>
     * <th>Required</th>
     * </tr>
     *
     * <tr>
     * <td><a name="name">name</a></td>
     * <td>The name, relative to <a href="#srcdir">srcdir</a>, of the XML file
     *     which defines the resources.</td>
     * <td>Yes</td>
     * </tr>
     *
     * <tr>
     * <td><a name="className">className</a></td>
     * <td>The name of the class to be generated, including the package, but
     *     not including any locale suffix. By default, the class name is
     *     derived from the name of the source file, for example
     *     <code>happy/BirthdayResource_en_US.xml</code> becomes class
     *     <code>happy.BirthdayResource</code>.</td>
     * <td>No</td>
     * </tr>
     * <tr>
     *
     * <td><a name="cppClassName">cppClassName</a></td>
     * <td>The name of the C++ class to be generated.  By default, the class
     *     name is derived from the name of the source file, for example
     *     <code>happy/BirthdayResource_en_US.xml</code> becomes class
     *     <code>happy.BirthdayResource</code>.</td>
     * <td>No</td>
     * </tr>
     *
     * <tr>
     * <td><a name="baseClassName">baseClassName</a></td>
     * <td>The fully-qualified name of the base class of the resource bundle.
     *     Defaults to "org.eigenbase.resgen.ShadowResourceBundle".</td>
     * <td>No</td>
     * </tr>
     *
     * <tr>
     * <td><a name="cppBaseClassName">cppBaseClassName</a></td>
     * <td>The fully-qualified name of the base class of the resource bundle
     *     for C++.  Defaults to "ResourceBundle".</td>
     * <td>No</td>
     * </tr>
     *
     * </table>
     */
    public static class Include
    {
        public Include()
        {
        }
        ResourceGenTask root;
        /** Name of source file, relative to 'srcdir'. */
        String fileName;
        /** Class name. */
        String className;
        /** Base class. */
        String baseClassName;

        /** C++ Class name. */
        String cppClassName;
        /** C++ Base class. */
        String cppBaseClassName;

        void validate() throws BuildException
        {
            if (fileName == null) {
                throw new BuildException("You must specify attribute 'name'");
            }
        }

        void process(ResourceGen generator) throws BuildException
        {

            boolean outputJava = (root.mode != ResourceGenTask.MODE_CPP);
            boolean outputCpp = (root.mode != ResourceGenTask.MODE_JAVA);

            FileTask task;
            if (fileName.endsWith(".xml")) {
                task = generator.createXmlTask(this, fileName,
                                       className, baseClassName, outputJava,
                                       cppClassName, cppBaseClassName,
                                       outputCpp);
            } else if (fileName.endsWith(".properties")) {
                task = generator.createPropertiesTask(this, fileName);
            } else {
                throw new BuildException(
                            "File '" + fileName + "' is not of a supported " +
                            "type (.java or .properties)");
            }
            try {
                task.process(generator);
            } catch (IOException e) {
                e.printStackTrace();
                throw new BuildException(
                        "Failed while processing '" + fileName + "'", e);
            }
        }

        /** Sets <a href="#name">name</a>.
         *
         * @param name Name */
        public void setName(String name)
        {
            this.fileName = name;
        }

        /** Sets <a href="#className">className</a>.
         *
         * @param className Class name */
        public void setClassName(String className)
        {
            this.className = className;
        }

        /** Sets <a href="#baseClassName">baseClassName</a>.
         *
         * @param baseClassName Base class name */
        public void setBaseClassName(String baseClassName)
        {
            this.baseClassName = baseClassName;
        }

        String getBaseClassName()
        {
            return baseClassName;
        }

        /** Sets <a href="#cppClassName">cppClassName</a>.
         *
         * @param className C++ class name */
        public void setCppClassName(String className)
        {
            this.cppClassName = className;
        }

        /** Sets <a href="#cppBaseClassName">cppBaseClassName</a>.
         *
         * @param baseClassName Base C++ class name */
        public void setCppBaseClassName(String baseClassName)
        {
            this.cppBaseClassName = baseClassName;
        }

        String getCppBaseClassName()
        {
            return cppBaseClassName;
        }
    }
}

// End ResourceGenTask.java
