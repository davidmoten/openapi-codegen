#!/bin/bash
cd ../../openapi-directory
curr=`pwd`
find . -name "openapi.yaml"|sort|xargs -i grep -L "openapi: 3.1" "{}"|while read f; do
  cd "$curr"
  codegenc "$f" -DmaxClassNameLength=80 -DfailOnParseErrors=false
done
