# turin-maven-plugin
A Maven plugin to compile Turin files

```xml
<build>
    <plugins>
        <plugin>
            <groupId>me.tomassetti.turin</groupId>
            <artifactId>turin-maven-plugin</artifactId>
            <version>0.1-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals><goal>compile-turin</goal></goals>
                </execution>
            </executions>
            <configuration>
            </configuration>
        </plugin>
    </plugins>
</build>
```
