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

/**
 *
 * @author martin.bergljung@alfresco.com
 */
@Mojo(name = "it",
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        aggregator = true, // Only run against the top-level project in a Maven build
        requiresDependencyResolution = ResolutionScope.TEST)
public class IntegrationTestMojo extends AbstractRunMojo {

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
            copyTestClassesFromSubModules2Parent();
            boolean fork = true;
            startTomcat(fork);
            runIntegrationTests();
            stopTomcat();
        }
    }

    /**
     * In an AIO project copy all integration test (IT) test-classes from sub projects/modules
     * to the parent target/test-classes
     *
     * @throws MojoExecutionException
     */
    protected void copyTestClassesFromSubModules2Parent() throws MojoExecutionException {
        // Get sub-module names, so we can see where to copy test classes from
        List<String> childModules = project.getModules();
        if (childModules == null || childModules.size() <= 0) {
            // Running in a single JAR module, nothing to copy,
            // all test classes are already in top level target/test-classes
            return;
        }

        for (String module : childModules) {
            getLog().info("Copying integration test-classes (*IT.class) from module '" + module +
                    "' to parent target/test-classes");

            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-resources-plugin"),
                            version(MAVEN_RESOURCE_PLUGIN_VERSION)
                    ),
                    goal("copy-resources"),
                    configuration(
                            element(name("outputDirectory"), "${project.build.testOutputDirectory}"),
                            element(name("resources"),
                                    element(name("resource"),
                                            element(name("directory"), module + "/target/test-classes"),
                                            element(name("includes"),
                                                    element(name("include"), "**/*IT.class")
                                            ),
                                            element(name("filtering"), "false")
                                    )
                            )
                    ),
                    execEnv
            );
        }
    }

    protected void runIntegrationTests() throws MojoExecutionException {
        getLog().info("Executing integration tests (*IT.class)...");

        List<Dependency> failsafePluginDependencies = new ArrayList<Dependency>();
        failsafePluginDependencies.add(
                dependency("org.apache.maven.surefire", "surefire-junit47", "2.19.1"));

        Plugin failsafePlugin = plugin(
                groupId("org.apache.maven.plugins"),
                artifactId("maven-failsafe-plugin"),
                version("2.19.1"),
                failsafePluginDependencies
        );

        executeMojo(failsafePlugin,
                goal("integration-test"),
                configuration(
                        element(name("includes"),
                                element(name("include"), "**/IT*.class")
                        )
                ),
                execEnv
        );
    }

    protected void stopTomcat() throws MojoExecutionException {
        getLog().info("Stopping Tomcat...");

        Plugin tomcatPlugin = plugin(
                groupId("org.apache.tomcat.maven"),
                artifactId("tomcat7-maven-plugin"),
                version(MAVEN_TOMCAT7_PLUGIN_VERSION)
        );

        executeMojo(tomcatPlugin,
                goal("shutdown") ,
                configuration(),
                execEnv
        );
    }
}