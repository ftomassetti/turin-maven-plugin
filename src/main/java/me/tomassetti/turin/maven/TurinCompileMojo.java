package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.TurinFileWithSource;
import me.tomassetti.turin.parser.analysis.resolvers.*;
import me.tomassetti.turin.parser.analysis.resolvers.jar.JarTypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.Position;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compile Turin files
 */
@Mojo( name = "compile-turin", defaultPhase = LifecyclePhase.COMPILE )
public class TurinCompileMojo extends AbstractMojo
{

    @Parameter(required = true, readonly = true, property = "project")
    protected MavenProject project;

    @Parameter(required = true, readonly = true, property = "project.compileClasspathElements")
    protected List<String> classpathElements;

    public List<File> getTurinSourceDirs() {
        File srcMainTurin = new File(project.getBasedir(), "src/main/turin");
        if (srcMainTurin.exists()) {
            return ImmutableList.of(srcMainTurin);
        } else {
            return Collections.emptyList();
        }
    }

    public File targetDir() {
        File targetClasses = new File(project.getBasedir(), "target/classes");
        return targetClasses;
    }

    private void saveClassFile(ClassFileDefinition classFileDefinition) throws MojoFailureException {
        File output = null;
        try {
            output = new File(targetDir(), classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            output.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(classFileDefinition.getBytecode());
        } catch (IOException e) {
            String message = "Problem writing file "+output+": "+ e.getMessage();
            getLog().error(message);
            throw new MojoFailureException(message);
        }
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
        getLog().info("Turin Maven Plugin - Running on "+ project.getName());

        Parser parser = new Parser();

        // First we collect all TurinFiles and we pass it to the resolver
        List<TurinFileWithSource> turinFiles = new ArrayList<>();
        for (File sourceDir : getTurinSourceDirs()) {
            try {
                turinFiles.addAll(parser.parseAllIn(sourceDir));
            } catch (FileNotFoundException e){
                getLog().error("File not found: " + e.getMessage());
                throw new MojoFailureException("Turin files cannot be compiled: " + e.getMessage());
            } catch (IOException e) {
                getLog().error("IO problem: " + e.getMessage());
                throw new MojoFailureException("Turin files cannot be compiled: " + e.getMessage());
            } catch (RuntimeException e) {
                String message = "Turin file cannot be parsed: " + e.getMessage();
                getLog().error(message);
                throw new MojoFailureException(message);
            }
        }
        TypeResolver typeResolver = new ComposedTypeResolver(ImmutableList.<TypeResolver>builder()
            .add(JdkTypeResolver.getInstance())
            .addAll(project.getDependencyArtifacts().stream().map((da) -> {
                try {
                    return new JarTypeResolver(da.getFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()))
            .build());
        Resolver resolver = new ComposedResolver(ImmutableList.of(new InFileResolver(typeResolver), new SrcResolver(turinFiles.stream().map((tf)->tf.getTurinFile()).collect(Collectors.toList()))));

        // Then we compile all files
        // TODO consider classpath
        Compiler.Options options = new Compiler.Options();
        options.setClassPathElements(project.getDependencyArtifacts().stream().map((da) -> da.getFile().getPath()).collect(Collectors.toList()));
        Compiler instance = new Compiler(resolver, options);
        for (TurinFileWithSource turinFile : turinFiles) {
            ErrorCollector errorCollector = new ErrorCollector() {
                @Override
                public void recordSemanticError(Position position, String s) {
                    getLog().error("[" + turinFile.getSource().getPath()+"] Error at "+position + " : " + s);
                }
            };
            try {
                for (ClassFileDefinition classFileDefinition : instance.compile(turinFile.getTurinFile(), errorCollector)) {
                    saveClassFile(classFileDefinition);
                }
            } catch (RuntimeException e){
                String message = "Turin file cannot be compiled: " + e.getMessage();
                getLog().error(message);
                throw new MojoFailureException(message);
            }
        }
    }

}
