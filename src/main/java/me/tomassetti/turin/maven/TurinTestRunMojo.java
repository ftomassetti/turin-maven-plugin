package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.compiled.DirClassesTypeResolver;
import me.tomassetti.turin.testing.TestFinder;
import me.tomassetti.turin.testing.TurinTestRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.junit.runner.notification.RunNotifier;
import turin.test.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Run Turin tests
 */
@Mojo( name = "test-turin", defaultPhase = LifecyclePhase.TEST )
public class TurinTestRunMojo extends TurinMojo
{

    public static final String TESTS_ARE_FAILING_MSG = "Tests are failing.";

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


    private List<Class> collectTests(File dir, URLClassLoader classLoader) throws MojoExecutionException {
        try {
            TestFinder testFinder = new TestFinder();
            return testFinder.collectTests(dir, classLoader);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Unable to load class: " + e.getMessage(), e);
        }
    }

    private URLClassLoader getClassLoader() throws MojoExecutionException {
        URL[] depsUrls = project.getDependencyArtifacts().stream().map((da) -> {
            try {
                return da.getFile().toURL();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(new URL[]{});
        URLClassLoader depsClassLoader = new URLClassLoader(depsUrls);
        URLClassLoader codeClassLoader = null;
        try {
            codeClassLoader = new URLClassLoader(new URL[]{
                    new File(project.getBasedir(), "target/classes").toURL(),
                    new File(project.getBasedir(), "target/test-classes").toURL(),
            }, depsClassLoader);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Unable to configure the classloader: " + e.getMessage(), e);
        }
        return codeClassLoader;
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

        List<Class> testClasses = collectTests(targetDir(), getClassLoader());

        ConsoleRunListener consoleRunListener = new ConsoleRunListener(this);
        RunNotifier runNotifier = new RunNotifier();
        runNotifier.addListener(consoleRunListener);
        executeTests(testClasses, runNotifier);

        getLog().info("");
        getLog().info("Tests report:");
        getLog().info("=============");
        getLog().info("  OK: " + consoleRunListener.getSuccesses() + "/" +consoleRunListener.getTotal());
        getLog().info("  KO: " + consoleRunListener.getFailures() + "/" +consoleRunListener.getTotal());

        if (consoleRunListener.getFailures() > 0) {
            throw new MojoFailureException(TESTS_ARE_FAILING_MSG);
        }
    }

    private void executeTests(List<Class> testClasses, RunNotifier runNotifier) {
        for (Class clazz : testClasses) {
            new TurinTestRunner(this, clazz, getLog()).run(runNotifier);
        }
    }

}
