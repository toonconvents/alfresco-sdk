#This project is a workaround for [issue 420 - Make it possible to generate WARs without alfresco:run in SDK 3.0](https://github.com/Alfresco/alfresco-sdk/issues/420).

##What was added?
###Skip install of module dependency
Module dependencies can now take a boolean to determine at runtime if their install needs to be skipped.
Example:
```xml
<platformModules>
    <moduleDependency>
        <groupId>some.group.id</groupId>
        <artifactId>someArtifact</artifactId>
        <version>v1.0</version>
        <skipInstall>${skip.someArtifact.install}</skipInstall>
    </moduleDependency>
</platformModules>
```
###Copy of share-config-custom.xml dependent on maven.alfresco.enableTestProperties property
If you want to skip install of the test alfresco-global.properties and share-config-custom.xml set the property `maven.alfresco.enableTestProperties` to false.

##What else do I need to configure for a clean war to be built?
Introduce a skip property in the `jrebel-maven-plugin` in the build section of your sdk pom:
```xml
<!-- Hot reloading with JRebel -->
      <plugin>
        <groupId>org.zeroturnaround</groupId>
        <artifactId>jrebel-maven-plugin</artifactId>
        <version>${jrebel.version}</version>
        <executions>
          <execution>
            <id>generate-rebel-xml</id>
            <phase>process-resources</phase>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <skip>${skip.jrebel.plugin}</skip>
          <!-- For more information about how to configure JRebel plugin see:
               http://manuals.zeroturnaround.com/jrebel/standalone/maven.html#maven-rebel-xml -->
          <classpath>
            <fallback>all</fallback>
            <resources>
              <resource>
                <directory>${project.build.outputDirectory}</directory>
                <directory>${project.build.testOutputDirectory}</directory>
              </resource>
            </resources>
          </classpath>

          <!--
            alwaysGenerate - default is false
            If 'false' - rebel.xml is generated if timestamps of pom.xml and the current rebel.xml file are not equal.
            If 'true' - rebel.xml will always be generated
          -->
          <alwaysGenerate>true</alwaysGenerate>
        </configuration>
      </plugin>
```

Introduce a new profile in your sdk pom with the following properties set (including our new `skip.jrebel.plugin` property):
```xml
<profile>
      <!-- Build a releasable war that doesn't include local run config -->
      <id>build-wars-only</id>
      <properties>
        <!-- Do not start tomcat -->
        <maven.alfresco.startTomcat>false</maven.alfresco.startTomcat>
        <!-- disable h2 config -->
        <maven.alfresco.enableH2>fase</maven.alfresco.enableH2>
        <!-- We always need the Platform/Repo webapp - alfresco.war -->
        <maven.alfresco.enablePlatform>true</maven.alfresco.enablePlatform>
        <!-- We need Share webapp, so we got a UI for working with the Repo -->
        <maven.alfresco.enableShare>true</maven.alfresco.enableShare>
        <!-- We do not need to build solr files -->
        <maven.alfresco.enableSolr>false</maven.alfresco.enableSolr>
        <!-- Enable the REST API Explorer -->
        <maven.alfresco.enableApiExplorer>false</maven.alfresco.enableApiExplorer>
        <!-- Disable copying of hotswap config into war -->
        <maven.alfresco.copyHotswapAgentConfig>false</maven.alfresco.copyHotswapAgentConfig>
        <!-- Do not copy test properties -->
        <maven.alfresco.enableTestProperties>false</maven.alfresco.enableTestProperties>
        <!-- Do not include the local share logging config -->
        <maven.alfresco.useCustomShareLog4jConfig>false</maven.alfresco.useCustomShareLog4jConfig>
        <!-- Do not include local configs -->
        <env>prod</env>
        <!-- Do not generate jrebel rebel.xml file -->
        <skip.jrebel.plugin>true</skip.jrebel.plugin>
      </properties>
    </profile>
```