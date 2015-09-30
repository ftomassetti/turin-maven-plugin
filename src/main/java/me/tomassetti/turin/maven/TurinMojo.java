package me.tomassetti.turin.maven;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.TurinFileWithSource;
import me.tomassetti.turin.parser.analysis.resolvers.*;
import me.tomassetti.turin.parser.analysis.resolvers.compiled.JarTypeResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.Position;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TurinMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, property = "project")
    protected MavenProject project;

    @Parameter(required = true, readonly = true, property = "project.compileClasspathElements")
    protected List<String> classpathElements;

    public abstract List<File> getTurinSourceDirs();

    public abstract List<TypeResolver> extraTypeResolvers() throws MojoFailureException;

    protected void logException(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        getLog().debug("Exception logged " + errors.toString());
    }

    protected void compile() throws MojoFailureException {
        Parser parser = new Parser();

        // First we collect all TurinFiles and we pass it to the resolver
        List<TurinFileWithSource> turinFiles = new ArrayList<>();
        for (File sourceDir : getTurinSourceDirs()) {
            try {
                turinFiles.addAll(parser.parseAllIn(sourceDir));
            } catch (FileNotFoundException e){
                getLog().error("File not found: " + e.getMessage());
                logException(e);
                throw new MojoFailureException("Turin files cannot be compiled: " + e.getMessage());
            } catch (IOException e) {
                getLog().error("IO problem: " + e.getMessage());
                logException(e);
                throw new MojoFailureException("Turin files cannot be compiled: " + e.getMessage());
            } catch (RuntimeException e) {
                String message = "Turin file cannot be parsed: " + e.getMessage();
                getLog().error(message);
                logException(e);
                throw new MojoFailureException(message);
            }
        }
        List<JarTypeResolver> jarResolvers = new LinkedList<>();
        List<String> classpathElements = new LinkedList<>();
        for (Artifact artifact : project.getDependencyArtifacts()) {
            if (artifact.getFile() == null) {
                getLog().warn("Null artifact: " + artifact);
            } else {
                try {
                    jarResolvers.add(new JarTypeResolver(artifact.getFile()));
                    classpathElements.add(artifact.getFile().getPath());
                } catch (IOException e) {
                    throw new MojoFailureException("", e);
                }
            }
        }
        TypeResolver typeResolver = new ComposedTypeResolver(ImmutableList.<TypeResolver>builder()
            .add(JdkTypeResolver.getInstance())
            .addAll(extraTypeResolvers())
            .addAll(jarResolvers)
            .build());
        SymbolResolver resolver = new ComposedSymbolResolver(ImmutableList.of(new InFileSymbolResolver(typeResolver), new SrcSymbolResolver(turinFiles.stream().map((tf)->tf.getTurinFile()).collect(Collectors.toList()))));

        // Then we compile all files
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        options.setClassPathElements(classpathElements);
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
                String message = "Turin file " + turinFile.getSource() + " cannot be compiled: " + e.getMessage();
                getLog().error(message);
                logException(e);
                throw new MojoFailureException(message);
            }
        }
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

    protected abstract File targetDir();
}
