# turin-maven-plugin
A Maven plugin to compile [Turin](https://github.com/ftomassetti/turin-programming-language) files

You can add in your POM this:

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

The plugin will compile your Turin file under `src/main/turin` and put the generated classes under `target/classes`
