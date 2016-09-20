/**
 * Copyright (C) 2016 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.alfresco.maven.plugin;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

/**
 *
 * @author martin.bergljung@alfresco.com
 */
@Mojo(name = "runtest",
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        aggregator = true, // Only run against the top-level project in a Maven build
        requiresDependencyResolution = ResolutionScope.TEST)
public class RunTestMojo extends RunMojo {

    @Override
    public void execute() throws MojoExecutionException {
        execEnv = executionEnvironment(
                project,
                session,
                pluginManager
        );

        if (enableSolr) {
            unpackSolrConfig();
            fixSolrHomePath();
            replaceSolrConfigProperties();
            installSolr10InLocalRepo();
        }

        if (enableTestProperties) {
            copyAlfrescoGlobalProperties();
            renameAlfrescoGlobalProperties();
        }

        if (enablePlatform) {
            buildPlatformWar();
        }

        if (enableShare) {
            buildShareWar();
        }

        if (startTomcat) {
            checkDatabaseConfig();
            startTomcat(true);
            runIntegrationTests();
           // startTomcat(false);
        }
    }

    private void runIntegrationTests() throws MojoExecutionException {

    /*
        <dependencies>
        <dependency>
        <groupId>org.apache.maven.surefire</groupId>
        <artifactId>surefire-junit47</artifactId>
        <version>${maven.failsafe.version}</version>
        </dependency>
        </dependencies>
        <configuration>
        <groups>org.alfresco.test.annotation.IntegrationTest</groups>
        </configuration>
        <executions>
        <execution>
        <goals>
        <goal>integration-test</goal>
        </goals>
        <configuration>
        <includes>
        <include>** / *.class</include>
                            </includes>
                        </configuration>
                    </execution>
                </executions>
        */
        List<Dependency> failsafePluginDependencies = new ArrayList<Dependency>();
        failsafePluginDependencies.add(
                // Packaging goes faster with this lib
                dependency("org.apache.maven.surefire", "surefire-junit47", "2.19.1"));

        Plugin failsafePlugin = plugin(
                groupId("org.apache.maven.plugins"),
                artifactId("maven-failsafe-plugin"),
                version("2.19.1"),
                failsafePluginDependencies
        );

        PluginExecution itExecution = new PluginExecution();
        itExecution.setId("run-it");
        itExecution.addGoal("integration-test");
        itExecution.setConfiguration(
                configuration(
                        element(name("includes"),
                                element(name("include"), "**/*.class")
                        )
                ));
        failsafePlugin.addExecution(itExecution);

        executeMojo(failsafePlugin,
                goal("integration-test"),
                configuration(
                        element(name("groups"), "org.alfresco.test.annotation.IntegrationTest")
                ),
                execEnv
        );
    }
}
