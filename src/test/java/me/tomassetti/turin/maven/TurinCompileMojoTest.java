package me.tomassetti.turin.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class TurinCompileMojoTest extends AbstractMojoTestCase {

    public void testMojoGoal() throws Exception {
        File pom = new File(getBasedir(), "src/test/resources/unit/first.xml");
        TurinCompileMojo mojo = (TurinCompileMojo) lookupMojo("compile-turin", pom);
        assertNotNull(mojo);
    }

}
