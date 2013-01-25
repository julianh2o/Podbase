#!/bin/bash

SCRIPT_ROOT="$( cd "$( dirname "$0" )" && pwd )"
PROJECT_ROOT=$(cd "$SCRIPT_ROOT/.."; pwd)

function main() {
	SAVE=$(pwd)
	cd $PROJECT_ROOT
	getControllers | createTests
	cd $SAVE
}

function getControllers() {
	find ./app/controllers -type f | grep -v "CRUD"
}

function createTests() {
	while read controller
	do
		className=$(basename $controller .java)
		echo "Creating Test$className.java"
		cat $controller | createTest $className > ./test/Test$className.java
	done
}

function filterFunctions {
	grep "public static"
}

function extractFunctionNames {
	perl -pe "s/.* (.*?)\(.*?\).*/\1/"
}

function createTest {
	echo "import org.junit.Test;"
	echo
	echo "public class Test$1 {"
	sed s/$1/Test$1/ | filterFunctions | extractFunctionNames | ucfirst | wrapTestFunction
	echo "}"
}

function ucfirst() {
	perl -ne 'print ucfirst($_)'
}

function wrapTestFunction() {
	while read line
	do 
		echo
		echo -e "\t@Test"
		echo -e "\tpublic void test$line() {";
		echo -e "\t}";
	done
}

main