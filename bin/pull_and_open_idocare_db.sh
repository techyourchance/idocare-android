#!/bin/bash

rm  /tmp/idocare_db 2> /dev/null # remove the existing copy

adb "$@" pull /data/data/il.co.idocare/databases/idocare_db /tmp/idocare_db

if [ ! -f  /tmp/idocare_db ]; then
    echo "Failed to pull the database. Aborting."
    exit 1;
fi

open /Applications/sqlitebrowser.app  --args /tmp/idocare_db
