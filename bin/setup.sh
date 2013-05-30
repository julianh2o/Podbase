#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PODBASE_DIR="$(dirname "$SCRIPT_DIR")"

SRC=$PODBASE_DIR/conf/application.conf.example
DST=$PODBASE_DIR/conf/application.conf

if [ -a "$SRC" ]
then
    echo "Configuration already exsts: $DST"
else
    cp $SRC $DST
    echo "Created configuration file: $DST"
fi
