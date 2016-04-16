#!/usr/bin/env bash

echo '1. checking if everything is ok'

while getopts ":u:" option;
do
    case "$option" in
    u)
        echo "-u was triggered, User: $OPTARG" >&2
        user_master=$OPTARG
        ;;

    *)
        echo "Hmm, an invalid option was received." >&2
        echo "Here's the usage statement:" >&2
        echo -e '-u :user' >&2
        exit 1
        ;;
    esac
done

shift $((OPTIND-1))

echo -e '\n 2. checking if id_rsa.pub exists, otherwise generate \n'
if [ ! -f ~/.ssh/id_rsa.pub ]; then
    echo 'generating ssh key'
    ssh-keygen -t rsa
else
    echo 'Ok'
fi

echo -e '\n 3. trying to connect to the machines\n'

machines_ip=$@
for machine_ip in $machines_ip
do
    ssh-copy-id ${user_master}@${machine_ip}
done

echo -e '\n 4. creating the jar file \n'
sbt assembly
echo -e '\n 5. copying jar file to machines \n'

for machine_ip in $machines_ip
do
    scp target/scala-2.11/barista_snapshot.jar ${user_master}@${machine_ip}:~/
    ssh ${user_master}@${machine_ip} 'sudo mkdir -p /etc/barista/ && sudo mv ~/barista_snapshot.jar /etc/barista'
done

echo -e '\n 6. starting barista on every master node\n'
for machine_ip in $machines_ip
do
    ssh ${user_master}@${machine_ip} 'java -jar /etc/barista/barista_snapshot.jar'
done
