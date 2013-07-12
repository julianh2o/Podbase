#!/bin/bash

# bin folder (where this script is located)
BIN_DIR="$( cd "$( dirname "$0" )" && pwd )"

# the project root is one above the bin folder
# using a "realpath" hack to get rid of the .. in the path
PROJECT_ROOT=`cd "$BIN_DIR/..";pwd`

# we use this temp file so we can write back to the file that we're using as a source
TMP_FILE=$PROJECT_ROOT/tmp/.tmpheaderupdate

# relative to the project root
SOURCE_DIRECTORIES="app test"

# Specify here the extensions that you want processed and the comment to use with that extension
read -d '' COMMENT_SPECIFICATION <<"EOF"
java //
sh #
EOF

HEADER=`cat $PROJECT_ROOT/misc/code_header.txt`

function getDirectories() {
	for name in $SOURCE_DIRECTORIES
	do
		#echo $PROJECT_ROOT/$name
		echo $name
	done
}

function filterFiles() {
	while read path
	do
		ext=`extensionForPath $path`
		commentDelimiter=`getCommentDelimiter $ext`
		if [ -n commentDelimiter ]
		then
			echo $path
		fi
	done
}

# Fetches the list of relative paths to files that should be considered for adding headers
function getFiles() {
	for dir in `getDirectories`
	do
		find $dir -type f | filterFiles
	done
}

# Prepends the string argument to the beginning of each line in STDIN
# Note, this also adds an extra space inbetween
function prependLines() {
	sed 's/^/'$1' /'
}

# Given a file extension as an argument this parses the COMMENT_SPEC for the correct comment delimiter
function getCommentDelimiter() {
	echo "$COMMENT_SPECIFICATION" | grep ^$1 | awk '{print $2}' | sed -e 's/[\/&]/\\&/g'
}

# expects source file on stdin and extension as an argument
# returns the text of the file with the new header either prepended or replacing the old one
function replaceOrAddHeader() {
	commentDelimiter=`getCommentDelimiter $1`
	echo "$HEADER" | prependLines $commentDelimiter
	perl -ne 'if (!length $a && /^'$commentDelimiter'/) { } else { $a=1; print; }'
}

# lowercases a string
function toLowerCase() {
	tr '[:upper:]' '[:lower:]'
}

# returns the extension for a given path
function extensionForPath() {
	filename=$(basename "$1")
	ext="${filename##*.}"
	echo $ext | toLowerCase
}

# takes a list of files and adds the header to each one
function addHeaders() {
	while read f
	do
		echo "Adding Header to $f"
		extension=`extensionForPath $f`
		cat $f | replaceOrAddHeader $extension
		exit
		#cat $f | replaceOrAddHeader $extension > $TMP_FILE
		#mv $TMP_FILE $f
	done
}

getFiles | addHeaders
