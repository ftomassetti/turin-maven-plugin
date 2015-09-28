package me.tomassetti.turin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.DefaultArtifactHandlerStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class MyProjectStub extends MavenProjectStub {

    @Override
    public List<Dependency> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public File getBasedir() {
        return baseDir;
    }

    private File baseDir;

    public void addDependencyArtifact(String groupId, String artifactId, String version, String scope, File file) {
        ArtifactStub artifact = new ArtifactStub();
        artifact.setArtifactId(artifactId);
        artifact.setGroupId(groupId);
        artifact.setScope(scope);
        artifact.setType("jar");
        artifact.selectVersion(version);
        artifact.setFile(file);
        artifact.setArtifactHandler(new DefaultArtifactHandlerStub("jar"));

        HashSet<Artifact> artifacts = new HashSet<>(getDependencyArtifacts());
        artifacts.add(artifact);
        setDependencyArtifacts(artifacts);
    }

    public MyProjectStub(File pom, File baseDir) {
        this.baseDir = baseDir;
        setFile(pom);
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;
        try
        {
            model = pomReader.read( ReaderFactory.newXmlReader(pom));

            setModel(model);
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
        setGroupId( model.getGroupId() );
        setArtifactId( model.getArtifactId() );
        setVersion( model.getVersion() );
        setName( model.getName() );
        setUrl( model.getUrl() );
        setPackaging( model.getPackaging() );

        Build build = new Build();
        build.setFinalName( model.getArtifactId() );
        build.setDirectory( getBasedir() + "/target" );
        build.setSourceDirectory( getBasedir() + "/src/main/java" );
        build.setOutputDirectory( getBasedir() + "/target/classes" );
        build.setTestSourceDirectory( getBasedir() + "/src/test/java" );
        build.setTestOutputDirectory( getBasedir() + "/target/test-classes" );
        setBuild( build );

        List compileSourceRoots = new ArrayList<>();
        compileSourceRoots.add( getBasedir() + "/src/main/java" );
        setCompileSourceRoots( compileSourceRoots );

        List testCompileSourceRoots = new ArrayList();
        testCompileSourceRoots.add( getBasedir() + "/src/test/java" );
        setTestCompileSourceRoots( testCompileSourceRoots );

        if (this.getDependencyArtifacts() == null) {
            this.setDependencyArtifacts(new HashSet<>());
        }
    }

}
