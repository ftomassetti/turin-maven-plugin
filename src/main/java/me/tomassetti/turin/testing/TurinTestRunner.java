package me.tomassetti.turin.testing;

import me.tomassetti.turin.maven.TurinTestRunMojo;
import me.tomassetti.turin.parser.ast.FunctionDefinition;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class TurinTestRunner extends Runner {

    private TurinTestRunMojo turinTestRunMojo;
    private Class testClass;

    public TurinTestRunner(TurinTestRunMojo turinTestRunMojo, Class testClass) {
        this.turinTestRunMojo = turinTestRunMojo;
        this.testClass = testClass;
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
                runNotifier.fireTestFailure(new Failure(getDescription(), e));
            }
        } catch (NoSuchMethodException e) {
            turinTestRunMojo.getLog().warn("Skipping test class " + testClass.getCanonicalName() + " because it has no the expected invoke method");
            runNotifier.fireTestIgnored(getDescription());
        }
    }
}
