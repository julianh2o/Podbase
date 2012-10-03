#!/bin/bash

src="julian@podbase.pbrc.hawaii.edu:/export/terabyte/database/"
dst="./data/"

function main() {
	wget -O ./migrate/data.txt "http://podbase.pbrc.hawaii.edu/exporthelper.php?mode=txt"
	wget -O ./migrate/data.yaml "http://podbase.pbrc.hawaii.edu/exporthelper.php"

	cat $1 | doCopy
}

function doCopy() {
	rsync -s -r $src $dst --verbose --files-from=-
}


main $1
