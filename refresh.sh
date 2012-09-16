#!/bin/bash

PLAY=/home/julian/play/play
HOSTNAME=`hostname`
if [ "$HOSTNAME" == "ares" ]; then
    cd /home/julian/podbase
    $PLAY stop
    git pull
    $PLAY start --%prod
else
    ssh root@new.podbase.net /home/julian/podbase/refresh.sh
fi
