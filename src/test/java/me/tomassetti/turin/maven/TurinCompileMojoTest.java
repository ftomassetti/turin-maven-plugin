package me.tomassetti.turin.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TurinCompileMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

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
