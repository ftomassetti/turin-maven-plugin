package me.tomassetti.turin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Compile Turin files
 */
@Mojo( name = "compile-turin", defaultPhase = LifecyclePhase.COMPILE )
public class TurinCompileMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project.artifact}", readonly = true, required = true )
    private Artifact projectArtifact;

    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File outputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // TODO to be implemented
    }
}
