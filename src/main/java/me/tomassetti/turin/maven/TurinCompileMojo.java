package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Compile Turin files
 */
@Mojo( name = "compile-turin", defaultPhase = LifecyclePhase.COMPILE )
public class TurinCompileMojo extends TurinMojo {

    @Override
    public List<File> getTurinSourceDirs() {
        File srcMainTurin = new File(project.getBasedir(), "src/main/turin");
        if (srcMainTurin.exists()) {
            return ImmutableList.of(srcMainTurin);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TypeResolver> extraTypeResolvers() {
        return Collections.emptyList();
    }

    public File targetDir() {
        File targetClasses = new File(project.getBasedir(), "target/classes");
        return targetClasses;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project == null) {
            String message = "This task should be performed in a project (project not set)";
            getLog().error(message);
            throw new MojoFailureException(message);
        }
        if (project.getName() == null) {
            String message = "This task should be performed in a project (name not set)";
            getLog().error(message);
            throw new MojoFailureException(message);
        }
        if (project.getBasedir() == null) {
            String message = "This task should be performed in a project (basedir not set)";
            getLog().error(message);
            throw new MojoFailureException(message);
        }
        getLog().info("Turin Maven Plugin (Compile) - Running on "+ project.getName());

        compile();
    }

}
