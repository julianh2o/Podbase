#!/bin/bash

HOSTNAME=`hostname`
if [ "$HOSTNAME" == "ares" ]; then
    cd /home/julian/podbase
    play stop
    git pull
    play start --%prod
else
    ssh julian@new.podbase.net /home/julian/podbase/refresh.sh
fi
