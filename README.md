# Ollama Maven Plugin

A Maven plugin to automatically launch an [Ollama](https://ollama.com) instance using Docker as part of the Maven build lifecycle.

## Requirements

- Docker must be installed and running

## Building

- mvn clean install

## Usage

- The plugin can be added as part of a profile that launches ollama and then your own plugins/apps. Change the execution phase and goals as needed.
- The configuration allows for changing the Ollama image, mapped port, the container name and the model to pull in automatically if it isn't found.
- You can also specify via cleanOnStartup that the container should be stopped and removed every time the plugin starts, which can be useful for starting off on a clean slate every time.

```xml

<plugin>
    <groupId>com.github.davidbrrr</groupId>
    <artifactId>ollama-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>start-ollama</id>
            <phase>pre-integration-test</phase>
            <goals>
                <goal>start</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <image>ollama/ollama</image>
        <port>11434</port>
        <containerName>ollama-maven</containerName>
        <model>gemma3:latest</model>
        <preloadModel>true</preloadModel>
        <cleanOnStartup>false</cleanOnStartup>
    </configuration>
</plugin>
```


