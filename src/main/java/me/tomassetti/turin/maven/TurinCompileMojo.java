package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.ComposedResolver;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.analysis.resolvers.SrcResolver;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compile Turin files
 */
@Mojo( name = "compile-turin", defaultPhase = LifecyclePhase.COMPILE )
public class TurinCompileMojo extends AbstractMojo
{
    protected MavenProject project;

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
            String message = "This task should be performed in a project";
            getLog().error(message);
            throw new MojoFailureException(message);
        }
        getLog().info("Turin Maven Plugin - Running on "+ project.getName());

        Parser parser = new Parser();

        // First we collect all TurinFiles and we pass it to the resolver
        List<TurinFile> turinFiles = new ArrayList<>();
        for (File sourceDir : getTurinSourceDirs()) {
            try {
                turinFiles.addAll(parser.parseAllIn(sourceDir));
            } catch (FileNotFoundException e){
                getLog().error("File not found: " + e.getMessage());
                throw new MojoFailureException("Turin files cannot be compiled");
            } catch (IOException e) {
                getLog().error("IO problem: " + e.getMessage());
                throw new MojoFailureException("Turin files cannot be compiled");
            } catch (RuntimeException e) {
                String message = "Turin file cannot be parsed";
                getLog().error(message);
                throw new MojoFailureException(message);
            }
        }
        Resolver resolver = new ComposedResolver(ImmutableList.of(new InFileResolver(), new SrcResolver(turinFiles)));

        // Then we compile all files
        // TODO consider classpath
        Compiler instance = new Compiler(resolver, new Compiler.Options());
        for (TurinFile turinFile : turinFiles) {
            try {
                for (ClassFileDefinition classFileDefinition : instance.compile(turinFile)) {
                    saveClassFile(classFileDefinition);
                }
            } catch (RuntimeException e){
                String message = "Turin file cannot be compiled";
                getLog().error(message);
                throw new MojoFailureException(message);
            }
        }
    }
}
