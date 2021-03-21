#!/bin/bash
date=$(date +"%Y-%m-%d")
PATH = "tmp/pids/server.pid"
if [[ ! -f $PATH ]]; then
    rails s -p 12121 -b 127.0.0.1 -e production > /tmp/rails_server.log 2>&1 &
else if [[ ps -p $(cat $PATH)  ]]; then
    echo "Already running!"
else
    python3 /var/www/slackbot/alert.py "\n>*Ruby - API Server Down!*\n*Time:* $(date)"
fi