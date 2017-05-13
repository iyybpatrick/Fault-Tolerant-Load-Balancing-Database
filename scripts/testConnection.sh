#!/bin/bash

SCRIPT_HOME=/home/ubuntu/elasticDB/scripts

echo "SCRIPT_HOME is set to $SCRIPT_HOME"

MYSQL_HOME=$SCRIPT_HOME/../mysql

echo "MYSQL_HOME is set to $MYSQL_HOME"

source $SCRIPT_HOME/set_env.sh

# Check scp to all servers
echo "*** checking scp to all servers *********************************"

for i in "root"
do
for j in $MASTER ${SLAVE[@]} 
do
echo "$i at $j"
ssh -o StrictHostKeyChecking=no -o BatchMode=yes $i@$j "hostname"
done
done

