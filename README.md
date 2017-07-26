# APIconsumption
There are several Transit providers. And a provide can have many bus routes. A bus route has several bus stop on it. And there are buses on this route. This program can let us get how long it takes until the next bus leave from a specified bus stop (which is identified by a unique StopId) to one direction. 
## Prerequisites
You need to download JDK from Oracle. It is required to have all Java distributions in a single directory. In order to install Java 8, following files should be downloaded and moved to a single directory. </br>
* jdk-8u131-linux-x64.tar.gz
* jdk-8u131-linux-x64.tar.gz 
* jce_policy-8.zip
## Installation
The script needs to be run as root. You need to provide the JDK distribution file (tar.gz) and the Java Installation Directory. The default value for Java installation directory is "/usr/lib/jvm" </br> </br>
Example: Install Oracle JDK 8 </br>
- sudo ./install-java.sh -f ~/software/java/jdk-8u131-linux-x64.tar.gz
## Running the test 
### method 1 : Eclipse
1. select "import" to create new projects from an archive file or directory. 
2. then select "general" -> "the existing projects into workspace".
3. then select root directory of the projects (APIconsumption) to import.
4. then we set the "Run Configuration" -> "Metro Blue" "Station Platform 1" "south" to run
### method 2 : command line
1. export all resources required to run an application into a JAR file on the local file system. "Export" -> "java" -> "runnable jar file"
2. java -jar (path of the jar file) (three parameters)
