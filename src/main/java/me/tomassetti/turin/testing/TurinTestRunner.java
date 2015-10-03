package me.tomassetti.turin.testing;

import me.tomassetti.turin.maven.TurinTestRunMojo;
import me.tomassetti.turin.parser.ast.FunctionDefinition;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.maven.plugin.logging.Log;

public class TurinTestRunner extends Runner {

    private TurinTestRunMojo turinTestRunMojo;
    private Class testClass;
    private Log log;

    public TurinTestRunner(TurinTestRunMojo turinTestRunMojo, Class testClass, Log log) {
        this.turinTestRunMojo = turinTestRunMojo;
        this.testClass = testClass;
        this.log = log;
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass, testClass.getCanonicalName());
    }

    @Override
    public void run(RunNotifier runNotifier) {
        try {
            Method method = testClass.getMethod(FunctionDefinition.INVOKE_METHOD_NAME);
            try {
                runNotifier.fireTestStarted(getDescription());
                method.invoke(null);
                runNotifier.fireTestFinished(getDescription());
            } catch (IllegalAccessException e) {
                turinTestRunMojo.getLog().warn("Skipping test class " + testClass.getCanonicalName() + " because the expected invoke method is not accessible");
                runNotifier.fireTestIgnored(getDescription());
            } catch (InvocationTargetException e) {
                turinTestRunMojo.getLog().warn("Test failing " + testClass.getCanonicalName() + ": " + e.getMessage());
                turinTestRunMojo.getLog().warn("issue: " + e.getCause().getMessage());
                logException(e.getTargetException());
                runNotifier.fireTestFailure(new Failure(getDescription(), e));
            }
        } catch (NoSuchMethodException e) {
            turinTestRunMojo.getLog().warn("Skipping test class " + testClass.getCanonicalName() + " because it has no the expected invoke method");
            runNotifier.fireTestIgnored(getDescription());
        }
    }

    protected void logException(Throwable ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        log.debug("Exception logged " + errors.toString());
    }
}
