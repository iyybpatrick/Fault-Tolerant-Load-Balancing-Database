SCRIPT_HOME=/home/ubuntu/elasticDB/scripts

echo "SCRIPT_HOME is set to $SCRIPT_HOME"

MYSQL_HOME=$SCRIPT_HOME/../mysql

echo "MYSQL_HOME is set to $MYSQL_HOME"

source $SCRIPT_HOME/set_env.sh

mysql --user="$MYSQL_USERNAME" --password="MYSQL_PASSWORD" < /home/ubuntu/elasticDB/tpcw/mysql.sql

echo "clean mysql done"

cd /home/ubuntu/java-tpcw

ant gendb

