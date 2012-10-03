#!/bin/bash

src="julian@podbase.pbrc.hawaii.edu:/export/terabyte/database/"
dst="/home/julian/Desktop/migrate/images"

function main() {
	wget -O data.txt "http://podbase.pbrc.hawaii.edu/exporthelper.php?mode=txt"
	wget -O data.yaml "http://podbase.pbrc.hawaii.edu/exporthelper.php"
	cat $1 | doCopy
}

function doCopy() {
	rsync -s -r $src $dst --verbose --files-from=-
}


main $1
