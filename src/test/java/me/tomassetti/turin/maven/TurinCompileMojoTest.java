package me.tomassetti.turin.maven;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReaderFactory;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

public class TurinCompileMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    class MyProjectStub extends MavenProjectStub {

        public MyProjectStub(File pom) {
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
    public void testMojoGoal() throws Exception {
        File projectCopy = this.resources.getBasedir("project1");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        TurinCompileMojo mojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(mojo);

        mojo.setProject(new MyProjectStub(pom));

        // Create the Maven project by hand (...)
        //final MavenProject mvnProject = new MavenProject() ;
        //mvnProject.setFile( pom );
        //this.rule.setVariableValueToObject( mojo, "project", mvnProject );
        //assertNotNull( this.rule.getVariableValueFromObject( mojo, "project" ));

        // Execute the mojo
        //List<Resource> list = mvnProject.getResources();


        mojo.execute();
    }


}
