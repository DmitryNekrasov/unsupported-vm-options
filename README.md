# Unsupported OpenJDK vm options synchronization tool

This application adds new options from the new version of the JDK to the list of unsupported options.

To work, you need to run the application (`unsupported-vm-options-1.0-jar-with-dependencies.jar`) with three parameters:
* Path to build from old JDK version
* Path to build from new JDK version
* Path to the hotspot folder in the Zing project

Example (you can find it in the `example-run.sh` file):
```
#!/bin/bash
java -jar unsupported-vm-options-1.0-jar-with-dependencies.jar \
	/home/dnekrasov/dev/openjdk_builds/openjdk-17-fastdebug/ \
	/home/dnekrasov/dev/openjdk_builds/openjdk-19-fastdebug/ \
	/home/dnekrasov/dev/zing/openjdk/dev/hotspot/
```

If you want to edit main.kt file and rebuild the project, please use the next command:
```
mvn clean install
```