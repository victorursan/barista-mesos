# Barista Mesos Microservices [![Build Status](https://magnum.travis-ci.com/victorursan/barista-mesos-microservices.svg?&branch=master)](https://magnum.travis-ci.com/victorursan/barista-mesos-microservices)

## Setup
Create a Mesos Cluster. You can find an easy setup [here](https://github.com/victorursan/mesos-cluster-ansible).

Install ssh copy id:
```
brew install ssh-copy-id
```

## Deployment
Run the `deploy.sh` script:

```
./deploy.sh -u vagrant 10.1.1.11 10.1.1.12 10.1.1.13
```

where:

`-u vagrant` sets the `root` user to `vagrant` for Mesos Master nodes.

`10.1.1.11, 10.1.1.12, 10.1.1.13` are the Mesos Master’s IPs.

The `deploy.sh` script will:
  - creates a ssh key if you don't have one
  - connects via ssh with the nodes (you will be asked for the nodes root password)
  - builds project, the .jar file is in `./target/scala-2.11`
  - copies and runs the jar on every master node
