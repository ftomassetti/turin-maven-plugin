package me.tomassetti.turin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TurinCompileMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    class MyProjectStub extends MavenProjectStub {

        @Override
        public Set<Artifact> getDependencyArtifacts() {
            return Collections.emptySet();
        }

        @Override
        public List<Dependency> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        public File getBasedir() {
            return baseDir;
        }

        private File baseDir;

        public MyProjectStub(File pom, File baseDir) {
            this.baseDir = baseDir;
            setFile(pom);
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            Model model;
            try
            {
                model = pomReader.read( ReaderFactory.newXmlReader(pom ));

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

        }

    }

    @Test
    public void inSimpleCaseSourceDirsAreCalculated() throws Exception {
        File projectCopy = this.resources.getBasedir("project1");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        TurinCompileMojo mojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(mojo);

        this.rule.setVariableValueToObject( mojo, "project", new MyProjectStub(pom, projectCopy) );

        assertEquals(1, mojo.getTurinSourceDirs().size());
    }

    @Test(expected = MojoFailureException.class)
    public void aProjectWithBrokenFilesFails() throws Exception {
        File projectCopy = this.resources.getBasedir("project1");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        TurinCompileMojo mojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(mojo);

        this.rule.setVariableValueToObject( mojo, "project", new MyProjectStub(pom, projectCopy) );

        mojo.execute();
    }

    @Test
    public void aProjectWithCorrectFilesDoesNotFail() throws Exception {
        File projectCopy = this.resources.getBasedir("project2");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        TurinCompileMojo mojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(mojo);

        this.rule.setVariableValueToObject( mojo, "project", new MyProjectStub(pom, projectCopy) );

        mojo.execute();
    }

}
