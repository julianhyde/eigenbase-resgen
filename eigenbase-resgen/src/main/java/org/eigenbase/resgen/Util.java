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

import org.eigenbase.xom.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Miscellaneous utility methods for the <code>org.eigenbase.resgen</code>
 * package, all them <code>static</code> and package-private.
 *
 * @author jhyde
 */
abstract class Util {

    private static final Throwable[] emptyThrowableArray = new Throwable[0];

    /** loads URL into Document and returns set of resources */
    static ResourceDef.ResourceBundle load(URL url)
        throws IOException
    {
        return load(url.openStream());
    }

    /** loads InputStream and returns set of resources */
    static ResourceDef.ResourceBundle load(InputStream inStream)
        throws IOException
    {
        try {
            Parser parser = XOMUtil.createDefaultParser();
            DOMWrapper def = parser.parse(inStream);
            ResourceDef.ResourceBundle xmlResourceList = new
                ResourceDef.ResourceBundle(def);
            return xmlResourceList;
        } catch (XOMException err) {
            throw new IOException(err.toString());
        }
    }

    /**
     * Left-justify a block of text.  Line breaks are preserved, but long lines
     * are broken.
     *
     * @param pw where to output the formatted text
     * @param text the text to be written
     * @param linePrefix a string to prepend to each output line
     * @param lineSuffix a string to append to each output line
     * @param maxTextPerLine the maximum number of characters to place on
     *        each line, not counting the prefix and suffix.  If this is -1,
     *        never break lines.
     */
    static void fillText(
        PrintWriter pw, String text, String linePrefix, String lineSuffix,
        int maxTextPerLine)
    {
        int i = 0;
        for (;;) {
            int end = text.length();
            if (end <= i) {
                // Nothing left.  We're done.
                break;
            }

            if (i > 0) {
                // End the previous line and start another.
                pw.println(lineSuffix);
                pw.print(linePrefix);
            }

            int nextCR = text.indexOf("\r", i);
            if (nextCR >= 0 && nextCR < end) {
                end = nextCR;
            }
            int nextLF = text.indexOf("\n", i);
            if (nextLF >= 0 && nextLF < end) {
                end = nextLF;
            }

            if (maxTextPerLine > 0 && i + maxTextPerLine <= end) {
                // More than a line left.  Break at the last space before the
                // line limit.
                end = text.lastIndexOf(" ",i + maxTextPerLine);
                if (end < i) {
                    // No space exists before the line limit; look beyond it.
                    end = text.indexOf(" ",i);
                    if (end < 0) {
                        // No space anywhere in the line.  Take the whole line.
                        end = text.length();
                    }
                }
            }

            pw.print(text.substring(i, end));

            // The line is short enough.  Print it, and find where the next one
            // starts.
            i = end;
            while (i < text.length() &&
                   (text.charAt(i) == ' ' ||
                    text.charAt(i) == '\r' ||
                    text.charAt(i) == '\n')) {
                i++;
            }
        }
    }

    static URL stringToUrl(String strFile) throws IOException
    {
        try {
            File f = new File(strFile);
            return convertPathToURL(f);
        } catch (Throwable err) {
            throw new IOException(err.toString());
        }
    }

    /**
     * Creates a file-protocol URL for the given filename.
     */
    static URL convertPathToURL(File file)
    {
        try {
            String path = file.getAbsolutePath();
            // This is a bunch of weird code that is required to
            // make a valid URL on the Windows platform, due
            // to inconsistencies in what getAbsolutePath returns.
            String fs = System.getProperty("file.separator");
            if (fs.length() == 1)
            {
                char sep = fs.charAt(0);
                if (sep != '/')
                    path = path.replace(sep, '/');
                if (path.charAt(0) != '/')
                    path = '/' + path;
            }
            path = "file://" + path;
            return new URL(path);
        } catch (MalformedURLException e) {
            throw new java.lang.Error(e.getMessage());
        }
    }

    static String formatError(String template, Object[] args)
    {
        String s = template;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toString();
            s = replace(s, "%" + (i + 1), arg);
            s = replace(s, "%i" + (i + 1), arg);
        }
        return s;
    }

    /** Returns <code>s</code> with every instance of <code>find</code>
     * converted to <code>replace</code>. */
    static String replace(String s,String find,String replace) {
        // let's be optimistic
        int found = s.indexOf(find);
        if (found == -1) {
            return s;
        }
        StringBuffer sb = new StringBuffer(s.length());
        int start = 0;
        for (;;) {
            for (; start < found; start++) {
                sb.append(s.charAt(start));
            }
            if (found == s.length()) {
                break;
            }
            sb.append(replace);
            start += find.length();
            found = s.indexOf(find,start);
            if (found == -1) {
                found = s.length();
            }
        }
        return sb.toString();
    }

    /** Return <code>val</code> in double-quotes, suitable as a string in a
     * Java or JScript program.
     *
     * @param val the value
     * @param nullMeansNull whether to print a null value as <code>null</code>
     *   (the default), as opposed to <code>""</code>
     */
    static String quoteForJava(String val,boolean nullMeansNull)
    {
        if (val == null) {
            return nullMeansNull ? "null" : "";
        }
        String s0;
        s0 = replace(val, "\\", "\\\\");
        s0 = replace(val, "\"", "\\\"");
        s0 = replace(s0, "\n\r", "\\n");
        s0 = replace(s0, "\n", "\\n");
        s0 = replace(s0, "\r", "\\r");
        return "\"" + s0 + "\"";
    }

    static String quoteForJava(String val)
    {
        return quoteForJava(val,true);
    }

    /**
     * Returns a string quoted so that it can appear in a resource file.
     */
    static String quoteForProperties(String val) {
        String s0;
        s0 = replace(val, "\\", "\\\\");
//      s0 = replace(val, "\"", "\\\"");
//      s0 = replace(s0, "'", "''");
        s0 = replace(s0, "\n\r", "\\n");
        s0 = replace(s0, "\n", "\\n");
        s0 = replace(s0, "\r", "\\r");
        s0 = replace(s0, "\t", "\\t");
        return s0;
    }

    static final char fileSep = System.getProperty("file.separator").charAt(0);

    static String fileNameToClassName(String fileName, String suffix) {
        String s = fileName;
        s = removeSuffix(s, suffix);
        s = s.replace(fileSep, '.');
        s = s.replace('/', '.');
        int score = s.indexOf('_');
        if (score >= 0) {
            s = s.substring(0,score);
        }
        return s;
    }

    static String fileNameToCppClassName(String fileName, String suffix) {
        String s = fileName;
        s = removeSuffix(s, suffix);

        int pos = s.lastIndexOf(fileSep);
        if (pos >= 0) {
            s = s.substring(pos + 1);
        }

        int score = s.indexOf('_');
        if (score >= 0) {
            s = s.substring(0, score);
        }
        return s;
    }

    static String removeSuffix(String s, final String suffix) {
        if (s.endsWith(suffix)) {
            s = s.substring(0,s.length()-suffix.length());
        }
        return s;
    }

    /**
     * Given <code>happy/BirthdayResource_en_US.xml</code>,
     * returns the locale "en_US".
     */
    static Locale fileNameToLocale(String fileName, String suffix) {
        String s = removeSuffix(fileName, suffix);
        int score = s.indexOf('_');
        if (score <= 0) {
            return null;
        } else {
            String localeName = s.substring(score + 1);
            return parseLocale(localeName);
        }
    }

    /**
     * Parses 'localeName' into a locale.
     */
    static Locale parseLocale(String localeName) {
        int score1 = localeName.indexOf('_');
        String language, country = "", variant = "";
        if (score1 < 0) {
            language = localeName;
        } else {
            language = localeName.substring(0, score1);
            if (language.length() != 2) {
                return null;
            }
            int score2 = localeName.indexOf('_',score1 + 1);
            if (score2 < 0) {
                country = localeName.substring(score1 + 1);
                if (country.length() != 2) {
                    return null;
                }
            } else {
                country = localeName.substring(score1 + 1, score2);
                if (country.length() != 2) {
                    return null;
                }
                variant = localeName.substring(score2 + 1);
            }
        }
        return new Locale(language,country,variant);
    }

    /**
     * Given "happy/BirthdayResource_fr_FR.properties" and ".properties",
     * returns "happy/BirthdayResource".
     */
    static String fileNameSansLocale(String fileName, String suffix) {
        String s = removeSuffix(fileName, suffix);
        // If there are directory names, start reading after the last one.
        int from = s.lastIndexOf(fileSep);
        if (from < 0) {
            from = 0;
        }
        while (from < s.length()) {
            // See whether the rest of the filename after the current
            // underscore is a valid locale name. If it is, return the
            // segment of the filename before the current underscore.
            int score = s.indexOf('_', from);
            Locale locale = parseLocale(s.substring(score+1));
            if (locale != null) {
                return s.substring(0,score);
            }
            from = score + 1;
        }
        return s;
    }

    /**
     * Converts a chain of {@link Throwable}s into an array.
     */
    static Throwable[] toArray(Throwable err)
    {
        ArrayList list = new ArrayList();
        while (err != null) {
            list.add(err);
            err = getCause(err);
        }
        return (Throwable[]) list.toArray(emptyThrowableArray);
    }

    private static final Class[] emptyClassArray = new Class[0];

    private static Throwable getCause(Throwable err) {
        if (err instanceof InvocationTargetException) {
            return ((InvocationTargetException) err).getTargetException();
        }
        try {
            Method method = err.getClass().getMethod(
                    "getCause", emptyClassArray);
            if (Throwable.class.isAssignableFrom(method.getReturnType())) {
                return (Throwable) method.invoke(err, new Object[0]);
            }
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
        try {
            Method method = err.getClass().getMethod(
                    "getNestedThrowable", emptyClassArray);
            if (Throwable.class.isAssignableFrom(method.getReturnType())) {
                return (Throwable) method.invoke(err, new Object[0]);
            }
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
        return null;
    }

    /**
     * Formats an error, which may have chained errors, as a string.
     */
    static String toString(Throwable err)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Throwable[] throwables = toArray(err);
        for (int i = 0; i < throwables.length; i++) {
            Throwable throwable = throwables[i];
            if (i > 0) {
                pw.println();
                pw.print("Caused by: ");
            }
            pw.print(throwable.toString());
        }
        return sw.toString();
    }

    static void printStackTrace(Throwable throwable, PrintWriter s) {
        Throwable[] stack = Util.toArray(throwable);
        PrintWriter pw = new DummyPrintWriter(s);
        for (int i = 0; i < stack.length; i++) {
            if (i > 0) {
                pw.println("caused by");
            }
            stack[i].printStackTrace(pw);
        }
        pw.flush();
    }

    static void printStackTrace(Throwable throwable, PrintStream s) {
        Throwable[] stack = Util.toArray(throwable);
        PrintStream ps = new DummyPrintStream(s);
        for (int i = 0; i < stack.length; i++) {
            if (i > 0) {
                ps.println("caused by");
            }
            stack[i].printStackTrace(ps);
        }
        ps.flush();
    }

    static void generateCommentBlock(
            PrintWriter pw,
            String name,
            String text,
            String comment)
    {
        final String indent = "    ";
        pw.println(indent + "/**");
        if (comment != null) {
            fillText(pw, comment, indent + " * ", "", 70);
            pw.println();
            pw.println(indent + " *");
        }
        pw.print(indent + " * ");
        fillText(
            pw,
            "<code>" + name + "</code> is '<code>"
                + StringEscaper.xmlEscaper.escapeString(text) + "</code>'",
            indent + " * ", "", -1);
        pw.println();
        pw.println(indent + " */");
    }

    /**
     * Returns the class name without its package name but with a locale
     * extension, if applicable.
     * For example, if class name is <code>happy.BirthdayResource</code>,
     * and locale is <code>en_US</code>,
     * returns <code>BirthdayResource_en_US</code>.
     */
    static String getClassNameSansPackage(String className, Locale locale) {
        String s = className;
        int lastDot = className.lastIndexOf('.');
        if (lastDot >= 0) {
            s = s.substring(lastDot + 1);
        }
        if (locale != null) {
            s += '_' + locale.toString();
        }
        return s;
    }

    protected static String removePackage(String s)
    {
        int lastDot = s.lastIndexOf('.');
        if (lastDot >= 0) {
            s = s.substring(lastDot + 1);
        }
        return s;
    }

    /**
     * So we know to avoid recursively calling
     * {@link Util#printStackTrace(Throwable,java.io.PrintWriter)}.
     */
    static class DummyPrintWriter extends PrintWriter {
        public DummyPrintWriter(Writer out) {
            super(out);
        }
    }

    /**
     * So we know to avoid recursively calling
     * {@link Util#printStackTrace(Throwable,PrintStream)}.
     */
    static class DummyPrintStream extends PrintStream {
        public DummyPrintStream(OutputStream out) {
            super(out);
        }
    }
}

// End Util.java
