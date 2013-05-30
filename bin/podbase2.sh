#!/bin/bash

PLAY=/home/imgup/play/play
HOSTNAME=`hostname`
if [ "$HOSTNAME" == "podbase2" ]; then
    cd /home/imgup/podbase
    $PLAY stop
    rm server.pid
    git pull
    $PLAY dependencies
    $PLAY start --%prod
else
    ssh julian@podbase2.pbrc.hawaii.edu "sudo /home/julian/podbase/bin/refresh.sh"
fi
