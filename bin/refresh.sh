#!/bin/bash

PLAY=/home/julian/play/play
HOSTNAME=`hostname`
if [ "$HOSTNAME" == "ares" ]; then
    cd /home/julian/podbase
    $PLAY stop
    rm server.pid
    git pull
    $PLAY dependencies
    $PLAY start --%prod
else
    ssh root@new.podbase.net /home/julian/podbase/bin/refresh.sh
fi
