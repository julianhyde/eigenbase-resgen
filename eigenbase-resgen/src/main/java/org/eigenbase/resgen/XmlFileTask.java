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

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Ant task which processes an XML file and generates a C++ or Java class from
 * the resources in it.
 *
 * @author jhyde
 */
class XmlFileTask extends FileTask
{
    final String baseClassName;
    final String cppBaseClassName;

    XmlFileTask(ResourceGenTask.Include include, String fileName,
              String className, String baseClassName, boolean outputJava,
              String cppClassName, String cppBaseClassName, boolean outputCpp)
    {
        this.include = include;
        this.fileName = fileName;
        this.outputJava = outputJava;
        if (className == null) {
            className = Util.fileNameToClassName(fileName, ".xml");
        }
        this.className = className;
        if (baseClassName == null) {
            baseClassName = "org.eigenbase.resgen.ShadowResourceBundle";
        }
        this.baseClassName = baseClassName;

        this.outputCpp = outputCpp;
        if (cppClassName == null) {
            cppClassName = Util.fileNameToCppClassName(fileName, ".xml");
        }
        this.cppClassName = cppClassName;
        if (cppBaseClassName == null) {
            cppBaseClassName = "ResourceBundle";
        }
        this.cppBaseClassName = cppBaseClassName;
    }

    void process(ResourceGen generator) throws IOException {
        URL url = Util.convertPathToURL(getFile());
        ResourceDef.ResourceBundle resourceList = Util.load(url);
        if (resourceList.locale == null) {
            throw new BuildException(
                    "Resource file " + url + " must have locale");
        }

        ArrayList localeNames = new ArrayList();
        if (include.root.locales == null) {
            localeNames.add(resourceList.locale);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(include.root.locales,",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                localeNames.add(token);
            }
        }

        if (!localeNames.contains(resourceList.locale)) {
            throw new BuildException(
                    "Resource file " + url + " has locale '" +
                    resourceList.locale +
                    "' which is not in the 'locales' list");
        }

        Locale[] locales = new Locale[localeNames.size()];
        for (int i = 0; i < locales.length; i++) {
            String localeName = (String) localeNames.get(i);
            locales[i] = Util.parseLocale(localeName);
            if (locales[i] == null) {
                throw new BuildException(
                        "Invalid locale " + localeName);
            }
        }


        if (outputJava) {
            generateJava(generator, resourceList, null);
        }

        generateProperties(generator, resourceList, null);

        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            if (outputJava) {
                generateJava(generator, resourceList, locale);
            }
            generateProperties(generator, resourceList, locale);
        }

        if (outputCpp) {
            generateCpp(generator, resourceList);
        }
    }

    private void generateProperties(
            ResourceGen generator,
            ResourceDef.ResourceBundle resourceList,
            Locale locale) {
        String fileName = Util.getClassNameSansPackage(className, locale) + ".properties";
        File file = new File(getResourceDirectory(), fileName);
        File srcFile = locale == null ?
            getFile() :
            new File(getSrcDirectory(), fileName);
        if (file.exists()) {
            if (locale != null) {
                if (file.equals(srcFile)) {
                    // The locale.properties file already exists, and the
                    // source and target locale.properties files are the
                    // same. No need to create it, or even to issue a warning.
                    // We were only going to create an empty file, anyway.
                    return;
                }
            }
            if (file.lastModified() >= srcFile.lastModified()) {
                generator.comment(file + " is up to date");
                return;
            }
            if (!file.canWrite()) {
                generator.comment(file + " is read-only");
                return;
            }
        }
        generator.comment("Generating " + file);
        final FileOutputStream out;
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new BuildException("Error while writing " + file, e);
        }
        PrintWriter pw = new PrintWriter(out);
        try {
            if (locale == null) {
                generateBaseProperties(resourceList, pw);
            } else {
                generateProperties(pw, file, srcFile, locale);
            }
        } finally {
            pw.close();
        }
    }


    /**
     * Generates a properties file containing a line for each resource.
     */
    private void generateBaseProperties(
        ResourceDef.ResourceBundle resourceList,
        PrintWriter pw)
    {
        String fullClassName = getClassName(null);
        pw.println("# This file contains the resources for");
        pw.println("# class '" + fullClassName + "'; the base locale is '" +
                resourceList.locale + "'.");
        pw.println("# It was generated by " + ResourceGen.class);

        pw.println("# from " + getFileForComments());
        if (include.root.commentStyle !=
            ResourceGenTask.COMMENT_STYLE_SCM_SAFE)
        {
            pw.println("# on " + new Date().toString() + ".");
        }
        pw.println();
        for (int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];
            final String name = resource.name;
            if (resource.text == null) {
                throw new BuildException(
                        "Resource '" + name + "' has no message");
            }
            final String message = resource.text.cdata;
            if (message == null) {
                continue;
            }
            if (count(resource.text.cdata, '\'') % 2 != 0) {
                System.out.println(
                    "WARNING: The message for resource '" + resource.name
                        + "' has an odd number of single-quotes. These should"
                        + " probably be doubled (to include an single-quote in"
                        + " a message) or closed (to include a literal string"
                        + " in a message).");
            }
            pw.println(name + "=" + Util.quoteForProperties(message));
        }
        pw.println("# End " + fullClassName + ".properties");
    }

    /**
     * Returns the number of occurrences of a given character in a string.
     *
     * <p>For example, {@code count("foobar", 'o')} returns 2.
     *
     * @param s String
     * @param c Character
     * @return Number of occurrences
     */
    private int count(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Generates a properties file for a given locale. If there is a source
     * file for the locale, it is copied. Otherwise generates a file with
     * headers but no resources.
     *
     * @param pw Output file writer
     * @param targetFile the locale-specific output file
     * @param srcFile The locale-specific properties file, e.g.
     *   "source/happy/BirthdayResource_fr-FR.properties". It may not exist,
     *   but if it does, we copy it.
     * @param locale Locale, never null
     * @pre locale != null
     */
    private void generateProperties(
        PrintWriter pw,
        File targetFile,
        File srcFile,
        Locale locale)
    {
        if (srcFile.exists() && srcFile.canRead() && !targetFile.equals(srcFile)) {
            try {
                final FileReader reader = new FileReader(srcFile);

                final char[] cbuf = new char[1000];
                int charsRead;
                while ((charsRead = reader.read(cbuf)) > 0) {
                    pw.write(cbuf, 0, charsRead);
                }
                return;
            } catch (IOException e) {
                throw new BuildException("Error while copying from '" +
                    srcFile + "'");
            }
        }

        // Generate an empty file.
        String fullClassName = getClassName(locale);
        pw.println("# This file contains the resources for");
        pw.println("# class '" + fullClassName + "' and locale '" + locale + "'.");
        pw.println("# It was generated by " + ResourceGen.class);
        pw.println("# from " + getFileForComments());
        if (include.root.commentStyle !=
            ResourceGenTask.COMMENT_STYLE_SCM_SAFE)
        {
            pw.println("# on " + new Date().toString() + ".");
        }
        pw.println();
        pw.println("# This file is intentionally blank. Add property values");
        pw.println("# to this file to override the translations in the base");
        String basePropertiesFileName = Util.getClassNameSansPackage(className, locale) + ".properties";
        pw.println("# properties file, " + basePropertiesFileName);
        pw.println();
        pw.println("# End " + fullClassName + ".properties");
    }

    private String getClassName(Locale locale) {
        String s = className;
        if (locale != null) {
            s += '_' + locale.toString();
        }
        return s;
    }

    protected void generateCpp(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList)
    {
        String defaultExceptionClass = resourceList.cppExceptionClassName;
        String defaultExceptionLocation = resourceList.cppExceptionClassLocation;
        if (defaultExceptionClass != null &&
            defaultExceptionLocation == null) {
            throw new BuildException(
                "C++ exception class is defined without a header file location in "
                + getFile());
        }

        for (int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            if (resource.text == null) {
                throw new BuildException(
                    "Resource '" + resource.name + "' has no message");
            }

            if (resource instanceof ResourceDef.Exception) {
                ResourceDef.Exception exception =
                    (ResourceDef.Exception)resource;

                if (exception.cppClassName != null &&
                    (exception.cppClassLocation == null &&
                     defaultExceptionLocation == null)) {
                    throw new BuildException(
                        "C++ exception class specified for "
                        + exception.name
                        + " without specifiying a header location in "
                        + getFile());
                }

                if (defaultExceptionClass == null &&
                    exception.cppClassName == null) {
                    throw new BuildException(
                        "No exception class specified for "
                        + exception.name
                        + " in "
                        + getFile());
                }
            }
        }


        String hFilename = cppClassName + ".h";
        String cppFileName = cppClassName + ".cpp";

        File hFile = new File(include.root.dest, hFilename);
        File cppFile = new File(include.root.dest, cppFileName);

        boolean allUpToDate = true;

        if (!checkUpToDate(generator, hFile)) {
            allUpToDate = false;
        }

        if (!checkUpToDate(generator, cppFile)) {
            allUpToDate = false;
        }

        if (allUpToDate && !include.root.force) {
            return;
        }

        generator.comment("Generating " + hFile);

        final FileOutputStream hOut;
        try {
            makeParentDirs(hFile);

            hOut = new FileOutputStream(hFile);
        } catch (FileNotFoundException e) {
            throw new BuildException("Error while writing " + hFile, e);
        }

        String className = Util.removePackage(this.className);
        String baseClassName = Util.removePackage(this.cppBaseClassName);

        PrintWriter pw = new PrintWriter(hOut);
        try {
            final CppHeaderGenerator gen =
                new CppHeaderGenerator(getFile(), hFile,
                className, baseClassName, defaultExceptionClass);
            configureCommentStyle(gen);
            gen.generateModule(generator, resourceList, pw);
        } finally {
            pw.close();
        }

        generator.comment("Generating " + cppFile);

        final FileOutputStream cppOut;
        try {
            makeParentDirs(cppFile);

            cppOut = new FileOutputStream(cppFile);
        } catch (FileNotFoundException e) {
            throw new BuildException("Error while writing " + cppFile, e);
        }

        pw = new PrintWriter(cppOut);
        try {
            final CppGenerator gen =
                new CppGenerator(getFile(), cppFile, className, baseClassName,
                    defaultExceptionClass, hFilename);
            configureCommentStyle(gen);
            gen.generateModule(generator, resourceList, pw);
        } finally {
            pw.close();
        }
    }
}

// End XmlFileTask.java
