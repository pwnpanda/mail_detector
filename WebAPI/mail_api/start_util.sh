#!/bin/bash
date=$(date +"%Y-%m-%d")
file_path="tmp/pids/server.pid"

function doCheck {
    if [[ ! -f $file_path ]]; then
        return 1
    fi
    PID=$(cat $file_path)
    echo $PID
    RES=$(ps -p $PID)
    STATUS=$($?)
    echo $STATUS
    return $STATUS
}

if [ doCheck == 0 ]; then
    echo "Already running!"
elif [[ ! -f $file_path ]]; then
    rails s -p 12121 -b 127.0.0.1 -e production > /tmp/rails_server.log 2>&1 &
    echo "Started!"
else
    python3 /var/www/slackbot/alert.py "\n>*Ruby - API Server Down!*\n*Time:* $(date)"
fi