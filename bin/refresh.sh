#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PODBASE_DIR="$(dirname "$SCRIPT_DIR")"
PLAY=/home/imgup/play/play

cd $PODBASE_DIR
$PLAY stop
rm server.pid
git pull
$PLAY dependencies
$PLAY start --%prod
