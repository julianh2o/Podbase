#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PODBASE_DIR="$(dirname "$SCRIPT_DIR")"

cp $PODBASE_DIR/conf/application.conf.example $PODBASE_DIR/conf/application.conf
