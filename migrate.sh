#!/bin/bash

src="julian@podbase.pbrc.hawaii.edu:/export/terabyte/database/"
dst="./data/"

function main() {
	wget -O data.txt "http://podbase.pbrc.hawaii.edu/exporthelper.php?mode=txt"
	wget -O data.yaml "http://podbase.pbrc.hawaii.edu/exporthelper.php"

	cat data.txt | doCopy
}

function doCopy() {
	rsync -s -r $src $dst --verbose --files-from=-
}


main
