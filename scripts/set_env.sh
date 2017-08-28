#!/bin/bash

set -o allexport

MYSQL_USERNAME='root'
MYSQL_PASSWORD='TigerBit!2016'

# HOSTS
#vm0=35.162.86.105
#vm1=54.204.168.204
#vm2=35.161.215.21

MASTER=35.162.86.105
SLAVE=(54.204.168.204)

# Directories from which files are copied
WORKING_HOME=/tmp

set +o allexport

