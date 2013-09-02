#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PODBASE_DIR="$(dirname "$SCRIPT_DIR")"
PLAY=/home/imgup/play/play

echo "Using: $PODBASE_DIR"
cd $PODBASE_DIR
pwd
$PLAY stop
rm server.pid
git pull
$PLAY dependencies
$PLAY evolutions:apply
nohup $PLAY start --%prod
#tail -f logs/system.out logs/application.log
