#!/bin/bash

PLAY=/home/julian/play/play
HOSTNAME=`hostname`
if [ "$HOSTNAME" == "podbase2" ]; then
    cd /home/julian/podbase
    $PLAY stop
    rm server.pid
    git pull
    $PLAY dependencies
    $PLAY start --%prod
else
    ssh julian@podbase2.pbrc.hawaii.edu /home/julian/podbase/bin/refresh.sh
fi
