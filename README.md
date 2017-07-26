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