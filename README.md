#Barista-mesos-microservices
=============================
## Setup
* Generate de .jar file
`sbt assembly`
* Copy on every master node the .jar file found in ./target/scala-2.11/barista_snapshot.jar
* Run the jar file on the lead node
`java -jar barista_snapshot.jar`
* To test the framework, on your host machine, go on 
`http://10.1.1.11:9000/barista` (replace the ip with your lead ip, you should get the slave nodes resources)
