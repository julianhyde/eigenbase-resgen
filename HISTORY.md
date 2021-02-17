# Eigenbase-resgen release history

For a full list of releases, see <a href="https://github.com/julianhyde/eigenbase-resgen/releases">github</a>.

## <a href="https://github.com/julianhyde/eigenbase-resgen/releases/tag/eigenbase-resgen-1.3.7">1.3.7</a> / 2021-02-17

* Upgrade junit to 4.13.1, xerces to 2.12.0
* Publish releases to <a href="https://search.maven.org/artifact/net.hydromatic/eigenbase-resgen">Maven Central</a>
  (previous releases are in <a href="http://www.conjars.org/">Conjars</a>)
* Sign jars
* Change maven groupId from "eigenbase" to "net.hydromatic"
* Upgrade to eigenbase-xom 1.3.6.

## <a href="https://github.com/julianhyde/eigenbase-resgen/releases/tag/eigenbase-resgen-1.3.6">1.3.6</a> / 2014-01-13

* Upgrade to eigenbase-xom 1.3.4.

## <a href="https://github.com/julianhyde/eigenbase-resgen/releases/tag/eigenbase-resgen-1.3.5">1.3.5</a> / 2014-01-08

* Add release notes and history.
* Make ant an optional dependency for the OSGI bundle.
* Enable oraclejdk8 in Travis CI.

## <a href="https://github.com/julianhyde/eigenbase-resgen/releases/tag/eigenbase-resgen-1.3.4">1.3.4</a> / 2013-12-06

* Oops, need conjars repository for dependencies.

## <a href="https://github.com/julianhyde/eigenbase-resgen/releases/tag/eigenbase-resgen-1.3.3">1.3.3</a> / 2013-12-06

* Switch to conjars as distribution repository.
* Enable maven-release-plugin.
* Changes made to POM to make the packaged jar a valid OSGI bundle.
* Enable Travis CI.
* Remove Intellij files from git.
* Remove files that can be generated from pom.
* Add content to README.
* Publish sources & javadoc.
* Migrate from DynamoBI.

## Origins

ResGen was derived from the
MonRG utility in the <a href="http://mondrian.pentaho.com">Mondrian project</a> (an open-source OLAP server).
Next, it was part of the Eigenbase project (hence the name).
Now it is a standalone library, maintained by Julian Hyde, and hosted at
<a href="http://github.com/julianhyde/eigenbase-resgen">github</a>.</p>
