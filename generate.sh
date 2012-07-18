# Generates ResourceDef.java and resource.dtd. These files are checked
# into git. You should invoke this script to re-generate them when you
# modify # Resource.xml, and check in the new versions.
exec java -cp ../eigenbase-xom/target/classes \
    org.eigenbase.xom.MetaGenerator \
    src/main/java/org/eigenbase/resgen/Resource.xml \
    src/main/java

# end
