package org.eigenbase.resgen;

import org.apache.maven.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Resource generation maven plugin.
 *
 * @goal resgen
 * @requiresProject false
 * @requiresDependencyResolution runtime
 */
public class ResourceGenMojo extends AbstractMojo {

    ResourceGenTask antTask = new ResourceGenTask();

    /**
     * @parameter expression="${srcDir}" default-value="${basedir}/src/main/resources"
     */
    File srcDir;

    /**
     * @parameter expression="${outDir}" default-value="${project.build.directory}/generated-sources/resgen"
     * @required
     */
    File outDir;

    /**
     * @parameter expression="${resDir}" default-value="${project.build.directory}/generated-resources/resgen"
     * @required
     */
    File resDir;

    /**
     * @parameter expression="${style}
     */
    String style;

    /**
     * @parameter expression="${locales}
     */
    String locales;

    /**
     * @parameter expression="${includes}
     */
    String[] includes;


    /**
     * The current project representation.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        resDir.mkdirs();
        outDir.mkdirs();

        antTask.setSrcdir(srcDir);
        antTask.setResdir(resDir);
        antTask.setDestdir(outDir);
        antTask.setStyle(style);
        antTask.setLocales(locales);

        for(String include : includes) {
            ResourceGenTask.Include i = new ResourceGenTask.Include();
            i.setName(include);
            antTask.addInclude(i);
        }

        antTask.execute();

        project.addCompileSourceRoot(outDir.getAbsolutePath());

        org.apache.maven.model.Resource res = new org.apache.maven.model.Resource();
        res.setDirectory(resDir.getAbsolutePath());
        project.addResource(res);
    }
}
