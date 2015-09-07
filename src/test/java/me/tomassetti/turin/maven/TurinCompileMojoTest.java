package me.tomassetti.turin.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TurinCompileMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Test
    public void testMojoGoal() throws Exception {
        File projectCopy = this.resources.getBasedir("project1");
        File pom = new File(projectCopy, "pom.xml");
        assertTrue(pom.exists());
        TurinCompileMojo mojo = (TurinCompileMojo) this.rule.lookupMojo("compile-turin", pom);
        assertNotNull(mojo);
        mojo.execute();
    }


}
