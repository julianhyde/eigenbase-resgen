[![Build Status](https://travis-ci.org/julianhyde/eigenbase-resgen.png)](https://travis-ci.org/julianhyde/eigenbase-resgen)

# eigenbase-resgen

Generator of type-safe wrappers for Java resource files.

# Prerequisites

Eigenbase-resgen requires git,
maven (3.2.1 or later),
and JDK 8 or later.

# Download and build

```bash
$ git clone git://github.com/julianhyde/eigenbase-resgen.git
$ cd eigenbase-resgen
$ mvn install
```

# Introduction

<p>The Eigenbase Resource Generator (eigenbase-resgen, or ResGen for short)
helps you build and maintain internationalized applications in Java. From a
simple XML file, it generates classes to access those resources in a type-safe
manner. It is tightly integrated with <a href="https://ant.apache.org/">
ANT</a>, to make the development process painless; and it supports a variety of
schemes to determine the current locale.</p>

<p>Let's take a look at a simple example.</p>

<h2>Example</h2>

<p>The following example shows how you would define a simple resource file, generate
resource classes from it, and use those classes in your own code.</p>

<h3>Create a resource file</h3>

<p>First, create a resource file like the following, <code>BirthdayResource_en_US.xml</code>:</p>

<blockquote>
  <pre>&lt;?xml version=&quot;1.0&quot; ?&gt;
&lt;?xml-stylesheet type=&quot;text/xsl&quot; href=&quot;Resource.xsl&quot; ?&gt;
&lt;resourceBundle locale=&quot;en_US&quot;&gt;
  &lt;message name=&quot;HappyBirthday&quot;&gt;
    &lt;text&gt;Happy Birthday, {0}! You don''t look {1,number}.&lt;/text&gt;
  &lt;/message&gt;
  &lt;exception name=&quot;TooYoung&quot; className=&quot;RuntimeException&quot;&gt;
    &lt;text&gt;{0} has not been born yet.&lt;/text&gt;
  &lt;/exception&gt;
&lt;/resourceBundle&gt;</pre>
</blockquote>

<p dir="ltr">The top-level element of a resource file is always a <code>&lt;resourceBundle&gt;</code>, and the only necessary property
is the <code>locale</code> of the current file. Its children are a mixture of
<code>&lt;message&gt;</code> and <code>&lt;exception&gt;</code> elements. Each must have a
<code>name</code> attribute and a <code>&lt;text&gt;</code> child holding the message
string.</p>

<h3>Create ANT target</h3>

<p>Now modify your ANT build-file, <code>build.xml</code>, as follows:</p>
<blockquote>
  <pre>&lt;taskdef name=&quot;resgen&quot; classname=&quot;org.eigenbase.resgen.ResourceGenTask&quot;&gt;
  &lt;classpath path=&quot;lib/eigenbase-resgen.jar:lib/eigenbase-xom.jar&quot;/&gt;
&lt;/taskdef&gt;

&lt;target name=&quot;generate.resources&quot;&gt;
  &lt;resgen srcdir=&quot;source&quot; locales=&quot;en_US&quot;&gt;
    &lt;include name=&quot;happy/BirthdayResource_en_US.xml&quot;/&gt;
  &lt;/resgen&gt;
&lt;/target&gt;

&lt;target name=&quot;compile&quot; depends=&quot;generate.resources&quot;&gt;
  &lt;javac srcdir=&quot;source&quot; destdir=&quot;classes&quot;&gt;
    &lt;include name=&quot;happy/BirthdayResource*.java&quot;/&gt;
  &lt;/javac&gt;
  &lt;copy todir=&quot;classes&quot;&gt;
    &lt;fileset dir=&quot;source&quot; includes=&quot;**/*.properties&quot;/&gt;
  &lt;/copy&gt;
&lt;/target&gt;</pre>
</blockquote>

<p>I have assumed that your Java source files are held in the &quot;<code>source</code>&quot;
directory, and classes are compiled to <code>classes</code> directory, but this
ought to be easy to change. If you already have a target to compile Java source
files, you don't need the &quot;<code>compile</code>&quot; target; just add its contents to your own
target.</p>

<h3>Compile</h3>

<p>Build as follows. (You need 'ant' on your path, and you will need to edit the
project.classpath property in <code>build.xml</code>. You also need <code>eigenbase-xom.jar</code>, available from <a href="https://github.com/julianhyde/eigenbase-xom">github</a>.)</p>

<blockquote>
  <pre>$ ant
Buildfile: build.xml

generate.resources:
[resgen] Generating source/happy/BirthdayResource.java
[resgen] Generating source/happy/BirthdayResource_en_US.java
[resgen] Generating source/happy/BirthdayResource_en_US.properties

compile:
[javac] Compiling 2 source files to classes
[copy] Copying 1 files to classes

BUILD SUCCESSFUL

Total time: 3 seconds</pre>
</blockquote>

<p>Four files are generated.</p>

<p><code>source/happy/BirthdayResource.java</code>:</p>

<blockquote>
  <pre>package happy;

import java.io.IOException;
import java.util.Locale;
import org.eigenbase.resgen.*</a>;

class BirthdayResource extends ShadowResourceBundle {
    public BirthdayResource() throws IOException {
    }
    private static String baseName = &quot;happy.BirthdayResource&quot;;
    /**
      * Retrieves the singleton instance of {@link BirthdayResource}. If
      * the application has called {@link #setThreadLocale}, returns the
      * resource for the thread's locale.
      */
    public static synchronized BirthdayResource instance() {
        return (BirthdayResource) instance(baseName);
    }
    /**
      * Retrieves the instance of {@link BirthdayResource} for the given locale.
      */
    public static synchronized BirthdayResource instance(Locale locale) {
        return (BirthdayResource) instance(baseName, locale);
    }
    /** HappyBirthday is 'Happy Birthday, {0}! You don''t look {1,number} at all.' */
    public static final ResourceDefinition HappyBirthday = new ResourceDefinition(&quot;HappyBirthday&quot;, &quot;Happy Birthday, {0}! You don''t look {1,number} at all.&quot;);
    public static String getHappyBirthday(String p0, Number p1) {
        return HappyBirthday.instantiate(
            getInstance(), new Object[] {p0, p1}).toString();
    }
    /** TooYoung is '{0} has not been born yet.' */
    public static final ResourceDefinition TooYoung = new ResourceDefinition(&quot;TooYoung&quot;, &quot;{0} has not been born yet.&quot;);
    public static String getTooYoung(String p0) {
        return HappyBirthday.instantiate(
            getInstance(), new Object[] {p0}).toString();
    }
     public static RuntimeException newTooYoung(String p0) {
        return new RuntimeException(TooYoung.instantiate(
            getInstance(), new Object[] {p0}), null);
    }
    public static RuntimeException newTooYoung(String p0, Throwable err) {
        return new RuntimeException(TooYoung.instantiate(
            getInstance(), new Object[] {p0}), err);
    }
}</pre>
</blockquote>

<p><code>source/happy/BirthdayResource_en_US.java</code>:</p>

<blockquote>
  <pre>package happy;
import java.io.IOException;

public class BirthdayResource_en_US extends BirthdayResource {
    public BirthdayResource_en_US() throws IOException {}
}</pre>
</blockquote>

<p><code>source/happy/BirthdayResource.properties</code>:</p>

<blockquote>
  <pre>HappyBirthday=Happy Birthday, {0}! You don''t look {1,number}.
TooYoung={0} has not been born yet.</pre>
</blockquote>

<p><code>source/happy/BirthdayResource_en_US.properties</code>:</p>

<blockquote>
  <pre># This file is intentionally blank. Add property values
# to this file to override the translations in the base
# properties file, BirthdayResource.properties.</pre>
</blockquote>

<p>For each resource, a <code>get</code><code><i>Xxx</i>()</code> method is
generated to retrieve that resource in the current locale, substituting
parameters appropriately. For exception resources, an additional two <code>new<i>Xxx</i>()</code>
methods are generated to create (but not throw) an exception.</p>

<p>Tokens such as <code>{0}</code> and <code>{1,number}</code> in the message
are automatically converted to method parameters of the right type. This means
that if you ever change the parameters in your error message, or accidentally
delete it, you code will no longer build. (If your code doesn't compile, you can
fix the problem immediately; better that than getting a phone call, &quot;I just got
this really weird error...&quot;, in a few months time.)</p>

<p>Here's how you might use it in your code:</p>

<blockquote>
  <pre>import happy.BirthdayResource;

public class Birthday {
    static void wishHappyBirthday(String name, int age) {
        if (age &lt; 0) {
            throw BirthdayResource.newTooYoung(name);
        }
        System.out.println(BirthdayResource.getHappyBirthday(name, age));
    }
    public static void main(String[] args) {
        wishHappyBirthday(&quot;Fred&quot;, 33);
        wishHappyBirthday(&quot;Wilma&quot;, -3);
    }
}</pre>
</blockquote>

<p>This produces the following output.</p>

<blockquote>
    <pre>Happy Birthday, Fred! You don't look 33.
RuntimeException: Wilma has not been born yet.</pre>
</blockquote>

<p>So there are the basics. That was easy, wasn't it? Now let's look at how you can
tell the system to switch to another locale, and how you go about producing
resource files for that locale.</p>

<h2>Of locales and threads</h2>

<p>When you ask for a message, the system needs to know the locale in order to
get the right translation. There are several strategies for this.</p>
<p>The simplest strategy is to do nothing. Most applications run in the same locale as their
host machine, and so when the system calls <code>Locale.getDefault()</code>, it
will return the right answer.</p>

<p>You can switch locale by calling <code>Locale.getDefault(Locale newLocale)</code>,
but this call will affect other applications running in the same Java Virtual
Machine, and is not allowed in some application server environments.</p>

<p>ResGen provides a method
<code>ShadowResourceBundle.setThreadLocale(Locale)</code> to allow threads
to have different locales. Threads which are working in a different locale
should call this method at their entry point. For example:</p>
<blockquote>
  <pre>System.out.println(BirthdayResource.getHappyBirthday(&quot;Fred&quot;, 33));
ShadowResourceBundle.setThreadLocale(Locale.FR);
System.out.println(BirthdayResource.getHappyBirthday(&quot;Pierre&quot;, 22));</pre>
</blockquote>

<p>produces the output</p>

<blockquote>
    <pre>Happy Birthday, Fred! You don't look 33.
Bon anniversaire, Pi&egrave;rre! 22, quel bon &acirc;ge.</pre>
</blockquote>

<p>Threads which have not made this call will remain in the default locale.</p>

<p>This strategy may not be possible if the threading model is complex. Here,
you should use an explicit resource bundle object:</p>

<blockquote>
  <pre>BirthdayResource myResource = BirthdayResource.instance();
System.out.println(myResource.getHappyBirthday(&quot;Fred&quot;, 33));
myResource = BirthdayResource.instance(Locale.FR);
System.out.println(myResource.getHappyBirthday(&quot;Pierre&quot;, 22));</pre>
</blockquote>

<p>The problem is that the accessor methods (<code>getHappyBirthday</code>, and so forth) are
static. To make them non-static, change add <code>static=&quot;false&quot;</code> to the
<code>&lt;resgen&gt;</code> ANT task:</p>

<blockquote>
  <pre>&lt;target name=&quot;generate.resources&quot;&gt;
  &lt;resgen srcdir=&quot;source&quot; <font color="#FF0000"><i><b>style=&quot;dynamic&quot;</b></i></font>&gt;
    &lt;include name=&quot;happy/BirthdayResource_en_US.xml&quot;/&gt;
  &lt;/resgen&gt;
&lt;/target&gt;</pre>
</blockquote>

<h2>Translation</h2>

<p>So far, we've just been dealing with one resource file, and you're probably
wondering why you went to all the trouble of extracting your messages and
exception strings! Have no fear, there will be more. A typical development
process goes as follows.</p>

<p>First, you develop your application for a single locale, referred to as the
<dfn>base locale</dfn>. I have been assuming that
this American English (<code>en_US</code>), but you can develop in any locale
you choose.</p>

<p>You create an XML file for this language containing all the messages and
exceptions used by your application. Developers are often tempted to put in
hard-coded strings, but ResGen's tight integration with ANT makes it painless to
modify the XML file and re-generate the wrapper as you go.</p>

<p>So now you have an application in the beta stage. You're written most of the
code, are getting through the pile of bugs, and would like to translate the
product into French (<code>fr_FR</code>). Recall that in the above example, we
were dealing with the following set of files.</p>

<blockquote>
  <table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="AutoNumber1">
    <tr>
      <th>Source files</th>
      <td valign="top">happy/BirthdayResource_en_US.xml</td>
    </tr>
    <tr>
      <th>Generated files</th>
      <td valign="top">happy/BirthdayResource.java<br>
      happy/BirthdayResource.properties<br>
      happy/BirthdayResource_en_US.java<br>
      happy/BirthdayResource_en_US.properties</td>
    </tr>
    <tr>
      <th>Runtime files</th>
      <td valign="top">happy/BirthdayResource.class<br>
      happy/BirthdayResource.properties<br>
      happy/BirthdayResource_en_US.class<br>
      happy/BirthdayResource_en_US.properties</td>
    </tr>
  </table>
</blockquote>

<p>Do the following steps:</p>

<ol>
  <li>Copy <code>happy/BirthdayResource.properties</code>&nbsp; to <code>happy/BirthdayResource_fr_FR.properties</code>,
  and translate the messages as appropriate for the new locale:<blockquote>
  <pre>HappyBirthday=Bon anniversaire, {0}! {1,number}, c'est un bon age.
TooYoung={0} n'est pas encore n&eacute;(e).</pre>
</blockquote>
  <p>&nbsp;</li>
  <li>Add <code>happy/BirthdayResource_fr_FR.properties</code> to the ANT task:<blockquote>
    <pre>&lt;target name=&quot;generate.resources&quot;&gt;
  &lt;resgen srcdir=&quot;source&quot; <font color="#FF0000"><i>locales=&quot;en_US,fr_FR&quot;</i></font>&gt;
    &lt;include name=&quot;happy/BirthdayResource_en_US.xml&quot;/&gt;
    <font color="#FF0000"><i>&lt;include name=&quot;happy/BirthdayResource_fr_FR.properties&quot;/&gt;</i></font>
   &lt;/resgen&gt;
&lt;/target&gt;</pre>
    </blockquote></li>
   <li>Build.</li>
</ol>

<p>ResGen treats <code>happy/BirthdayResource_fr_FR.properties</code> as a source file.
It generates <code>happy/BirthdayResource_fr_FR.java</code> from it, and validates that
every resource in <code>happy/BirthdayResource_fr_FR.properties</code> exists in
<code>happy/BirthdayResource_en_US.xml</code>, and has the same number and types of parameters.</p>

<p>In this multi-language scenario, the source and generated files are as
follows (new files are shown in <i>italic</i>):</p><blockquote>

  <table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="AutoNumber1">
    <tr>
      <th>Source files</th>
      <td valign="top" dir="ltr">happy/BirthdayResource_en_US.xml<br>
      <i>happy/BirthdayResource_fr_FR.properties</i></td>
    </tr>
    <tr>
      <th>Generated files</th>
      <td dir="ltr" valign="top">happy/BirthdayResource.java<br>
      happy/BirthdayResource.properties<br>
      happy/BirthdayResource_en_US.java<br>
      happy/BirthdayResource_en_US.properties<br>
      <i>happy/BirthdayResource_fr_FR.java</i></td>
    </tr>
    <tr>
      <th>Runtime files</th>
      <td dir="ltr" valign="top">happy/BirthdayResource.class<br>
      happy/BirthdayResource.properties<br>
      happy/BirthdayResource_en_US.class<br>
      happy/BirthdayResource_en_US.properties<br>
      <i>happy/BirthdayResource_fr_FR.class<br>
      happy/BirthdayResource_fr_FR.properties</i></td>
    </tr>
  </table>
</blockquote>

<p>(Validation is not implemented yet. As well as detecting modified and deleted
messages, it should also have a mode which reminds us to add new messages.)</p>

<a name="cpp_resources"></a>

<h2>Styles of generated code</h2>

<p>ResGen can generate Java code in three styles: <code>static</code>, <code>
dynamic</code> and <code>functor</code>.</p>

<h4>Static style</h4>

<p>In the <code>static</code> style, the generated Java resource class contains
a resource member and several methods for each resource. In the example, the <i>
TooYoung</i> resource had a data member and three methods:</p>
<ul>
	<li><code>public static final ResourceDefinition TooYoung;</code></li>
	<li><code>public static String getTooYoung(String)</code></li>
	<li><code>public static RuntimeException newTooYoung(String)</code></li>
	<li><code>public static RuntimeException newTooYoung(String, Throwable)</code></li>
</ul>

<p>To change style, add an attribute to your ant target:</p>

<blockquote>
	<pre>&lt;resgen srcdir=&quot;source&quot; <font color="#FF0000"><i><b>style=&quot;functor&quot; </b></i></font>locales=&quot;en_US&quot;&gt;
    &lt;include name=&quot;happy/BirthdayResource_en_US.xml&quot;/&gt;
&lt;/resgen&gt;</pre>
</blockquote>

<h4>Dynamic style</h4>

<p>In the <code>dynamic</code> style, the same data member and methods are
generated, but they are not static. For example,</p>
<ul>
	<li><code>public final ResourceDefinition TooYoung;</code></li>
	<li><code>public String getTooYoung(String)</code></li>
	<li><code>public RuntimeException newTooYoung(String)</code></li>
	<li><code>public RuntimeException newTooYoung(String, Throwable)</code></li>
</ul>

<p>Because the methods are not static, they have different behavior if they are
invoked on different objects. Typically you have an instance of the resource
bundle for each locale. These instances are accessed via the <code>instance()</code>
and <code>instance(Locale)</code> methods in the resource bundle:</p>
<ul>
	<li><code>BirthdayResource.instance()</code> returns the instance of the
	resource bundle for the thread's default locale.</li>
	<li><code>BirthdayResource.instance(Locale)</code> returns the instance of
	the resource bundle for the given locale.</li>
</ul>

<p>Here's the same code example using <code>dynamic</code> resources:</p>
<blockquote>
  <pre>import happy.BirthdayResource;

public class Birthday {
    static void wishHappyBirthday(String name, int age) {
        if (age &lt; 0) {
            throw <font color="#FF0000"><i><b>BirthdayResource.instance().newTooYoung(name)</b></i></font>;
        }
        System.out.println(<font color="#FF0000"><i><b>BirthdayResource.instance().getHappyBirthday(name, age)</b></i></font>);
    }
    public static void main(String[] args) {
        wishHappyBirthday(&quot;Fred&quot;, 33);
        wishHappyBirthday(&quot;Wilma&quot;, -3);
    }
}</pre>
</blockquote>

<p>&nbsp;</p>

<h4>Functor style</h4>

<p>In the <code>functor</code> style, only one data member is generated per
resource, but the data member belongs to a class which has all of the necessary
accessor methods. For example,</p>
<ul>
	<li><code>public final _Def0 TooYoung;</code></li>
	<li><code>public class _Def0 extends ResourceDefinition {<br>
&nbsp;&nbsp;&nbsp; public String getTooYoung(String);<br>
&nbsp;&nbsp;&nbsp; public RuntimeException newTooYoung(String);<br>
&nbsp;&nbsp;&nbsp; public RuntimeException newTooYoung(String, Throwable);<br>
	}</code></li>
</ul>

<p>The accessor methods have the same purpose as the generated methods in <code>
static</code> or <code>dynamic</code> style, but have different names. The <code>
str</code> accessor method corresponds to <code>getTooYoung</code>, and <code>ex</code>
corresponds <code>newTooYoung</code>.</p>

<p>Let's see how the code example looks when rewritten to use functors.</p>
<blockquote>
  <pre>import happy.BirthdayResource;

public class Birthday {
    static void wishHappyBirthday(String name, int age) {
        if (age &lt; 0) {
            throw <font color="#FF0000"><i><b>BirthdayResource.instance().TooYoung.ex(name)</b></i></font>;
        }
        System.out.println(<font color="#FF0000"><i><b>BirthdayResource.instance().HappyBirthday.str(name, age)</b></i></font>);
    }
    public static void main(String[] args) {
        wishHappyBirthday(&quot;Fred&quot;, 33);
        wishHappyBirthday(&quot;Wilma&quot;, -3);
    }
}</pre>
</blockquote>

<p>The code is slightly more verbose, but functors have two advantages. First,
functors are genuine objects, so can be passed as callbacks. Suppose that you
are designing a library method which is to create, populate, and throw an
exception if it encounters an error, but which cannot be dependent upon a
particular resource file. You could design the method to receive a functor, and
the method could call the functor's <code>ex()</code> method if it has an error.</p>

<p>Second, they make it easy to figure out which pieces of code are using a
particular resource, because there is only one access point to that resource.
For example, every piece of code which uses the <i>TooYoung</i> resource must do
so via the <code>BirthdayResource.TooYoung</code> data member. If no code is
using the data member, you can safely obsolete the resource.</p>

<h2>C++ Resource Support</h2>

<p>ResGen also includes support for building and maintaining
internationalized applications in <a target="_self" href="overview_cpp.html">C++</a>.

<h2>Resource file format</h2>

<blockquote>
<table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="AutoNumber2">
  <tr>
    <th>Element</th>
    <th>Attributes</th>
  </tr>
  <tr>
    <td><code>&lt;resourceBundle&gt;</code></td>
    <td><p>The top-level element.</p>

	<p>Attributes:</p>

	<blockquote>
		<table  border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="table2">
			<tr>
				<th>name</th>
				<th>description</th>
			</tr>
			<tr>
				<td><b><code>locale</code></b></td>
				<td>The locale of resources in this file. Required.</td>
			</tr>
			<tr>
				<td><b><code>style</code></b></td>
				<td>If <code>dynamic</code> (the default),
		generate several accessor methods per resource;
      if <code>functor</code>, generate one member per resource, with several
		methods. </td>
			</tr>
			<tr>
				<td><b><code>exceptionClassName</code></b></td>
				<td>The default class of exception to generate.
      Must be qualified by package name, unless it is in the package <code>
      java.lang</code>. Not required; default value is &quot;java.lang.RuntimeException&quot;.
      The <code>className</code> attribute of <code>&lt;exception&gt;</code> overrides
      this for a specific exception.</td>
			</tr>
			<tr>
				<td><b><code>cppNamespace</code></b></td>
				<td>The namespace used for
      generated C++ files.  Only used if resources are generated in
      C++ mode.  Optional.</td>
			</tr>
			<tr>
				<td><b><code>cppCommonInclude</code></b></td>
				<td>The name of a header file to
      include at the start of all generated C++ files.  Optional. Only used if
      resources are generated in C++ mode.</td>
			</tr>
			<tr>
				<td><b><code>cppExceptionClassName</code></b></td>
				<td>The name of the
      exception class to be returned by exception resources.
      Optional. Only used if resources are generated in C++ mode.</td>
			</tr>
			<tr>
				<td><b><code>cppExceptionClassLocation</code></b></td>
				<td>The name of
      the header file to include.  Should define the class referred to
      in cppExceptionClassName.  Required if cppExceptionClassName is
      given.</td>
			</tr>
		</table>
	</blockquote>
    <p>Children:</p>
    <ul>
      <li>Zero or more <code>&lt;message&gt;</code> or <code>&lt;exception&gt;</code> elements.</li>
    </ul>
    </td>
  </tr>
  <tr>
    <td><code>&lt;message&gt;</code></td>
    <td><p>Defines a localizable message.</p>

	<p>Attributes:</p>

	<blockquote>
		<table  border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="table4">
			<tr>
				<th>name</th>
				<th>description</th>
			</tr>
			<tr>
				<td><b><code>name</code></b></td>
				<td>The identifier of the message. Becomes the name of the
      property in the generated <code>.properties</code> file. Required, must be
      unique within the resource file, and must be a valid Java identifer.</td>
			</tr>
		</table>
	</blockquote>
    <p>Children:</p>
    <ul>
      <li>Zero or more <code>&lt;property&gt;</code> elements, defining properties of
		the resource. These properties will be accessible at runtime via the
		<a href="api/org/eigenbase/resgen/ResourceDefinition.html#getProperties()">
		ResourceDefinition.getProperties()</a> method.</li>
		<li>A single <code>&lt;text&gt;</code> element, holding the text of the message. </li>
    </ul>
    </td>
  </tr>
  <tr>
    <td>&lt;exception&gt;</td>
    <td>Defines an exception and its associated message.<p>Attributes:</p>

	<blockquote>
		<table  border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111">
			<tr>
				<th>name</th>
				<th>description</th>
			</tr>
			<tr>
				<td><b><code>name</code></b></td>
				<td>The identifier of the exception. Becomes the name of the
      property in the generated <code>.properties</code> file. Required, must be
      unique within the resource file, and must be a valid Java identifer.</td>
			</tr>
			<tr>
				<td><b><code>className</code></b></td>
				<td>The type of exception to generate. Must be fully qualified,
				unless it is in the package <code>java.lang</code>. If not<br>
      specified, the resource bundle's default exception class is used.</td>
			</tr>
			<tr>
				<td><b><code>cppClassName</code></b></td>
				<td>The name of the C++
      exception class returned by this exception.  Either this attribute
      or <code>&lt;resourceBundle&gt;'s cppExceptionClassName</code>
      must be defined.</td>
			</tr>
			<tr>
				<td><b><code>cppClassLocation</code></b></td>
				<td>The name of a C++
      header file to be included.  Required if
      <code>cppClassName</code> is used.</td>
			</tr>
			<tr>
				<td><b><code>cppChainExceptions</code></b></td>
				<td>If <code>false</code>
      (the default), only a basic constructor is need.  If
      <code>true</code> the basic and chained constructors are
      required (see the section on <a href="#cpp_resources">C++
      resources</a>.</td>
			</tr>
		</table>
	</blockquote>
    <p>Children:</p>
    <ul>
      <li>A single <code>&lt;text&gt;</code> element, holding the text of the message. </li>
    </ul>
    </td>
  </tr>
  <tr>
    <td><code>&lt;text&gt;</code></td>
    <td>The text of the message or exception. Should be in Java message format (see
    <a href="https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html">
    class java.text.MessageFormat</a> for more details).<p>If the message contains
    XML special characters such as '&lt;' and '&amp;', you may find it easier to
    enclose the message in <code>&lt;![CDATA[</code> ... <code>]]&gt;</code>.</td>
  </tr>
  <tr>
    <td>&lt;property&gt;</td>
    <td>Property of a resource.<p>Attributes:</p>

	<blockquote>
		<table  border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" id="table3">
			<tr>
				<th>name</th>
				<th>description</th>
			</tr>
			<tr>
				<td><code>name</code></td>
				<td>The name of the property.</td>
			</tr>
			</table>
	</blockquote>

	<p>Children:</p>
	<ul>
		<li>The value of the property.</li>
	</ul>
	</td>
  </tr>
</table>
</blockquote>

<h2>API</h2>

For details on the ResGen interfaces, please see the
<a href="http://www.hydromatic.net/resgen/apidocs/index.html">javadoc</a>.

<h2>Conclusion</h2>

<p>ResGen helps you build and maintain an internationalized application. Code
generation helps to leverage the power of the compiler: it detects parameters
which are missing or of the wrong type, spelling mistakes, and informational
messages which are being used to describe error conditions.</p>

# Use maven artifacts

Include the following in your `pom.xml`.

```xml
  <dependencies>
    <dependency>
      <groupId>net.hydromatic</groupId>
      <artifactId>eigenbase-resgen</artifactId>
      <version>1.3.7</version>
    </dependency>
  </dependencies>
```

# Release (for committers only)

Update version numbers in `README.md`, copyright date in `NOTICE`, and
add release notes to `HISTORY.md`.

Use JDK 8.

```bash
$ export GPG_TTY=$(tty)
$ git clean -nx
$ mvn clean
$ mvn release:clean
$ mvn -Prelease release:prepare
$ mvn -Prelease release:perform
```

# More information

* License: Apache License, Version 2.0
* Author: Julian Hyde
* Project page: http://www.hydromatic.net/resgen
* Source code: https://github.com/julianhyde/eigenbase-resgen
* Developers list:
  <a href="mailto:dev@calcite.apache.org">dev at calcite.apache.org</a>
  (<a href="https://mail-archives.apache.org/mod_mbox/calcite-dev/">archive</a>,
  <a href="mailto:dev-subscribe@calcite.apache.org">subscribe</a>)
* Continuous integration: https://travis-ci.org/julianhyde/eigenbase-resgen
* <a href="HISTORY.md">Release notes and history</a>
