#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d @heatblast-scheduler.json http://dev.banno.com:8080/v2/apps/heatblast-scheduler
