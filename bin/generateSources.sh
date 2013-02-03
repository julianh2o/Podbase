#!/bin/bash

BIN="$( cd "$( dirname "$0" )" && pwd )"
PROJECT_ROOT=$BIN/..

function main() {
	SOURCE=`getFiles | generate | entab | entab`
	fillTemplate "$SOURCE"
}

function entab() {
	sed 's/^/\t/g'
}

function fillTemplate() {
	echo "/* GENERATED SOURCE, DO NOT EDIT */";
	echo
	echo
	loadTemplate | while IFS='' read -r line
	do
		if [[ "$line" == *MARKER* ]]
		then
			echo "$1";
		else
			echo "$line";
		fi
		
	done
}

function loadTemplate() {
	cat $PROJECT_ROOT/public/javascripts/data/LinkTemplate.js
}

function generate() {
	while read line
	do
		SOURCE=`$BIN/generateLink.pl $line`
		if [ -n "$SOURCE" ]; then
			section "${line##*/}"
			echo "$SOURCE"
			echo
			echo
		fi
	done
}

function section() {
	separator
	echo "// $1"
	separator
}

function separator() {
	echo "// ############################################";
}

function getFiles() {
	find $PROJECT_ROOT/app/controllers -name "*.java"
}

if [[ "$1" == test ]]
then
	main
else
	main > $PROJECT_ROOT/public/javascripts/data/Link.js 
fi
