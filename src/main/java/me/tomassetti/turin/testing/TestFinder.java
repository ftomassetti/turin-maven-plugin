package me.tomassetti.turin.testing;

import org.apache.maven.plugin.MojoExecutionException;
import turin.test.Test;

import java.io.File;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

public class TestFinder {

    private void collectTests(File dir, URLClassLoader classLoader, String path, List<Class> testClasses) throws ClassNotFoundException {
        for (File child : dir.listFiles()) {
            if (child.isFile() && child.getName().endsWith(".class")) {
                String qName = qName(path, child);
                Class clazz = classLoader.loadClass(qName);
                Test annotation = (Test) clazz.getAnnotation(Test.class);
                if (annotation != null) {
                    testClasses.add(clazz);
                }
            } else if (child.isDirectory()) {
                collectTests(child, classLoader, path.isEmpty() ? child.getName() : path + "." + child.getName(), testClasses);
            }
        }
    }

    private String qName(String path, File file) {
        String baseName = file.getName();
        baseName = baseName.substring(0, baseName.length() - ".class".length());
        if (path.isEmpty()) {
            return baseName;
        } else {
            return path + "." + baseName;
        }
    }

    public List<Class> collectTests(File dir, URLClassLoader classLoader) throws ClassNotFoundException {
        List<Class> testClasses = new LinkedList<>();
        collectTests(dir, classLoader, "", testClasses);
        return testClasses;
    }
}
