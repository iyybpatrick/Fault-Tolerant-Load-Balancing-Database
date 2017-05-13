SCRIPT_HOME=/home/ubuntu/elasticDB/scripts

echo "SCRIPT_HOME is set to $SCRIPT_HOME"

MYSQL_HOME=$SCRIPT_HOME/../mysql

echo "MYSQL_HOME is set to $MYSQL_HOME"

source $SCRIPT_HOME/set_env.sh

echo "step 1 stop master/slave process"
ssh root@$MASTER "/etc/init.d/mysql stop"

for slave in ${SLAVE[@]}
do
echo "step 1 stop slave $slave"
ssh root@$slave "/etc/init.d/mysql stop"
done

echo "step 2 initialize master"
for target in $MASTER
do
ssh root@$target "$SCRIPT_HOME/initMaster.sh 1"
ssh root@$target "mysql --user="$MYSQL_USERNAME" --password="$MYSQL_PASSWORD" < $SCRIPT_HOME/grantMaster.sql"
done

echo "step 3 get master status"
string=$(ssh root@$MASTER "echo "show master status" | mysql --user="$MYSQL_USERNAME" --password="$MYSQL_PASSWORD"")
var=$(echo $string | awk -F" " '{print $1,$2,$3,$4,$5,$6}')
set -- $var
echo "bin is $5, pos is $6"
sed -e "s/vader-1-vm3/$MASTER/ig" $SCRIPT_HOME/slave-template.sql > $SCRIPT_HOME/grantSlave.sql
sed -i -e "s/mysql-bin.000002/$5/ig" $SCRIPT_HOME/grantSlave.sql
sed -i -e "s/=445/=$6/ig" $SCRIPT_HOME/grantSlave.sql

echo "step 4 restart slaves"
for (( i = 0 ; i < ${#SLAVE[@]} ; i++ ))
do
target=${SLAVE[$i]}
echo "restart $target"
scp $SCRIPT_HOME/grantSlave.sql root@$target:$SCRIPT_HOME/grantSlave.sql
servernum=$(echo $i+2 | bc)
ssh root@$target "$SCRIPT_HOME/initSlave.sh $MASTER `expr $servernum`"
ssh root@$target "mysql --user="$MYSQL_USERNAME" --password="$MYSQL_PASSWORD" < $SCRIPT_HOME/grantSlave.sql"
ssh root@$target "rm -rf $SCRIPT_HOME/grantSlave.sql"
done

rm -rf $SCRIPT_HOME/grantSlave.sql

echo "finish"

