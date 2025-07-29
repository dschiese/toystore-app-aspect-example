# Toystore Aspect-based logging - Example

This repository serves as a How-To-Guide to implement the aspects for system internal logging to provide user-centric, understandable and helpful explanations. The showcase for this approach was published as a demo paper and can be found under https://wse-research.org/publications/2025_ICWE_Schiese_LLM_debugging. 

## Research background

In our recent research, we were exploring the explainability of software systems, especially of distributed ones, as they are naturally more difficult to track and to understand. In our first approaches we focused on internal data like input and output of distributed services and tried to explain them by utilizing templates and different LLMs (see https://wse-research.org/publications/2024_ICWI_Schiese_Towards_LLM_generated_explanations_for_KGQA_systems). As the results were notable we further investigated the explanation capability by observing more atomic units in software systems (functions/methods). There we utilize input and output, just as we did for the components. Additionally, we also conduct source code and docstring if applicable. With these information given, we aim to produce even more detailed and helpful explanation and make further steps to explainable systems. 

If you use this workflow for your projects we'd like to hear about your findings and suggestions. If you have any question, don't hestitate to contact us.

# Requirements

Currently, the scope of our aspects is limited to API-based systems. Thus, an API-call is the scope of one logged process. The aspects realise this by _annotating_ Spring-annotated methods, in this case the following:

`PostMapping, RequestMapping, GetMapping, PutMapping, DeleteMapping, PatchMapping`.

This means, your app needs to implement these, otherwise the aspect won't work.

## Implementation

The implementation of the workflow can be realized as follows:

### Include the aspect and the plugin to your pom.xml

The first step is to include the aspect and the plugin to weave the aspect to your codebase. To do so, include the following snippets to your `pom.xml`

#### Plugin

Documentation: https://github.com/dev-aspectj/aspectj-maven-plugin

```
          <plugin>
            <groupId>dev.aspectj</groupId>
            <artifactId>aspectj-maven-plugin</artifactId>
            <version>1.14</version>
            <dependencies>
                <dependency>
                    <groupId>org.aspectj</groupId>
                    <artifactId>aspectjtools</artifactId>
                    <version>1.9.24</version>
                </dependency>
            </dependencies>
            <configuration>
                <complianceLevel>21</complianceLevel>
                <source>21</source>
                <target>21</target>
                <!-- Your AspectJ aspects as dependency -->
                <aspectLibraries>
                    <aspectLibrary>
                        <groupId>org.wseresearch</groupId>
                        <artifactId>generalizedaspect</artifactId>
                    </aspectLibrary>
                </aspectLibraries>
            <outxml>true</outxml>
            <outxmlfile>true</outxmlfile>
            <XaddSerialVersionUID>true</XaddSerialVersionUID>
            <showWeaveInfo>true</showWeaveInfo>
            <verbose>true</verbose>
            </configuration>
            <executions>
                <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>test-compile</goal>
                </goals>
                </execution>
            </executions>
        </plugin>
```

#### Dependency

The aspect that will be woven in, needs to be included as dependency as well as the weaver dependency.

```
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.9.24</version>
        </dependency>

        <dependency>
            <groupId>org.wseresearch</groupId>
            <artifactId>generalizedaspect</artifactId>
            <version>(,1.0.0)</version>
        </dependency>
```

#### Testing

To test the implementation, build the maven project with `mvn clean install`.

### Set up the proxy

The [proxy](https://github.com/WSE-research/logging_proxy) handles the logging of the methods to an external graph DBMS and increases your app's speed, as the logging of hundreds or thousands of methods takes some time.

FYI: Internally, the **generalizedaspect** (currently) has a hard coded endpoint to the proxy: `http://localhost:4000/`. 

#### Settings:

- `server.port` - Set the port
- `virtuoso.endpoint` - Set the virtuoso endpoint via jdbc. If you're using this app inside docker you need to link to the virtuoso instance, e.g., `virtuoso.endpoint=jdbc:virtuoso://virtuoso:1111` (with virtuoso being the name of the docker container)
- `virtuoso.username` and `virtuoso.password` - Set the username and the corresponding password. You'll need write permissions if not using dba user.
- `logging.queue.fixedRate` - Set the time between queue checks.

#### Build & run as jar

Build: `mvn clean install`
Run: `java -jar PATH_TO_JAR/logging_proxy-0.0.1.jar`

#### Build & run with docker

Build: `docker build .`
Run: `docker run`

For docker-compose, see below.

## Test: Query the logged data


## Explain your data using a web frontend





