package me.tomassetti.turin.maven;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class ConsoleRunListener extends RunListener {

    private TurinTestRunMojo turinTestRunMojo;
    private int successes = 0;
    private int failures = 0;

    public ConsoleRunListener(TurinTestRunMojo turinTestRunMojo) {
        this.turinTestRunMojo = turinTestRunMojo;
    }

    public int getSuccesses() {
        return successes;
    }

    public int getFailures() {
        return failures;
    }

    public int getTotal() {
        return successes + failures;
    }

    @Override
    public void testFinished(Description description) throws Exception {
        turinTestRunMojo.getLog().info(" [OK] " + description.getDisplayName());
        successes++;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        turinTestRunMojo.getLog().warn(" [KO] " + failure.getDescription().getDisplayName());
        failures++;
    }
}
