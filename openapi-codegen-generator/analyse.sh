#!/bin/bash
TIMESTAMP=`date -d "today" +"%Y%m%d%H%M"`
LOG="~/oc-$TIMESTAMP.log"
echo writing logs to $LOG
./analyse-base.sh 2&>$LOG
grep -e "^codegen" -e "ERROR.*java" -e "Caused by" $LOG|grep "^codegen" -A 1|grep -v "^--"|grep -v "^codegen" -B 1|grep -v "^--" &>~/oc-$TIMESTAMP-errors.log
