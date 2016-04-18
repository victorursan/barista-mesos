# Barista-mesos-microservices

## Setup
Create a mesos cluster. You can find an easy build [here](https://github.com/victorursan/mesos-cluster-ansible)

## Deployment
### Deploy using the included script
Make sure on OSX you have instaled `ssh-copy-id`

* Simply run the following command.

`./deploy.sh -u vagrant 10.1.1.11 10.1.1.12 10.1.1.13` (where -u is the flag for the root user on the mesos-master nodes, in this case is vagrant, and the ip's at the end are the addresses.)

This will do the following:
  - create a ssh key if you don't have one
  - connect via ssh with the nodes (you will be asked for the nodes root password)
  - create the .jar file from the project
  - copy and run the jar on every master node

### Deploy manualy
* Generate de .jar file.
`sbt assembly`
* Copy on every master node the .jar file found in ./target/scala-2.11/barista_snapshot.jar.
* Run the jar file on the lead node.
`java -jar barista_snapshot.jar`
* To test the framework, on your host machine, access 
`http://10.1.1.11:9000/barista` (replace the ip with your lead ip, you should get the slave nodes resources).
