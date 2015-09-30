package me.tomassetti.turin.testing;

import turin.test.Test;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestFinder {

    private void collectTests(File dir, URLClassLoader classLoader, String path, List<Class> testClasses) throws ClassNotFoundException {
        if (dir == null || dir.listFiles() == null) {
            return;
        }
        for (File child : dir.listFiles()) {
            if (child == null) {
                continue;
            }
            if (child.isFile() && child.getName() != null && child.getName().endsWith(".class")) {
                String qName = qName(path, child);
                Class clazz = classLoader.loadClass(qName);
                // they could have a different version of the annotation, so we check by name
                if (Arrays.stream(clazz.getAnnotations())
                        .filter((a)->a.annotationType().getCanonicalName().equals(Test.class.getCanonicalName()))
                        .findFirst()
                        .isPresent()) {
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
