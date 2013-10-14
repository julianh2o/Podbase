#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PODBASE_DIR="$(dirname "$SCRIPT_DIR")"
PLAY=/home/imgup/play/play

echo "Removing indecies.."
rm -rf $PODBASE_DIR/tmp/index/*

echo "Downloading indecies.."
scp -r podbase2.pbrc.hawaii.edu:/home/imgup/podbase/tmp/index/* $PODBASE_DIR/tmp/index/
