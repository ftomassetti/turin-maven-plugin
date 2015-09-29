package me.tomassetti.turin.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TurinTestRunMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Test
    public void runTests() throws Exception {
        File projectCopy = this.resources.getBasedir("project3");
        File pom = new File(projectCopy, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        TurinCompileMojo turinCompileMojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(turinCompileMojo);
        TurinTestCompileMojo turinTestCompileMojo = (TurinTestCompileMojo) this.rule.lookupMojo("test-compile-turin", pom);
        assertNotNull(turinTestCompileMojo);

        MyProjectStub project = new MyProjectStub(pom, projectCopy);
        project.addDependencyArtifact("junit", "junit", "4.12", "test", new File("src/test/resources/dependencies/junit-4.12.jar"));
        project.addDependencyArtifact("me.tomassetti", "turin-standard-library", "0.0.1-20150928-SNAPSHOT", null, new File("src/test/resources/dependencies/turin-standard-library-0.0.1-20150928-SNAPSHOT.jar"));
        this.rule.setVariableValueToObject( turinCompileMojo, "project", project );
        this.rule.setVariableValueToObject( turinTestCompileMojo, "project", project );

        turinCompileMojo.execute();
        turinTestCompileMojo.execute();
    }

}
