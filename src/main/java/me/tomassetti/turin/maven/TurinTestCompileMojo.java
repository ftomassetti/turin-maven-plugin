package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.compiled.DirClassesTypeResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Compile Turin tests
 */
@Mojo( name = "test-compile-turin", defaultPhase = LifecyclePhase.TEST_COMPILE )
public class TurinTestCompileMojo extends TurinMojo
{

    @Override
    public List<File> getTurinSourceDirs() {
        File srcMainTurin = new File(project.getBasedir(), "src/test/turin");
        if (srcMainTurin.exists()) {
            return ImmutableList.of(srcMainTurin);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TypeResolver> extraTypeResolvers() throws MojoFailureException {
        try {
            return ImmutableList.of(new DirClassesTypeResolver(new File(project.getBasedir(), "target/classes")));
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    public File targetDir() {
        File targetClasses = new File(project.getBasedir(), "target/test-classes");
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
        getLog().info("Turin Maven Plugin (Test) - Running on "+ project.getName());

        compile();
    }

}
