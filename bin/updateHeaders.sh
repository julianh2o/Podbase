#!/bin/bash

BIN="$( cd "$( dirname "$0" )" && pwd )"
PROJECT_ROOT=$BIN/..
TMP_FILE=$PROJECT_ROOT/tmp/.tmpheaderupdate

HEADER=`cat $PROJECT_ROOT/misc/code_header.txt`

function getFiles() {
	find -path ./modules -prune -o -name *.java -print
}

function prependComment() {
	sed 's/^/\/\/ /'
}

function replaceOrAddHeader() {
	echo "$HEADER" | prependComment
	perl -ne 'if (!length $a && /^\/\//) { } else { $a=1; print; }'
}

function addHeaders() {
	while read f
	do
		echo "Adding Header to $f"
		cat $f | replaceOrAddHeader > $TMP_FILE
		mv $TMP_FILE $f
	done
}

getFiles | addHeaders