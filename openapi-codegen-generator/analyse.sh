#!/bin/bash
curr=`pwd`
find . -name "openapi.yaml"|grep amazon|sort|while read f; do
  cd "$curr"
  echo "$f"
  temp=`codegen "$f"|tail -1`
  cd $temp && mvn -B compile 
done
