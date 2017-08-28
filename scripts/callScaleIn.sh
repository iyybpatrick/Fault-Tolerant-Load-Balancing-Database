#! /bin/bash

if [ $# -ne 1 ];
then
  echo 'usage: $0 [target db]'
   exit 1 
fi 

ssh root@$1 "/etc/init.d/mysql stop"