#!/bin/bash
curr=`pwd`
find . -name "openapi.yaml"|sort|while read f; do
  cd "$curr"
  echo "$f"
  temp=`codegen "$f"|tail -1`
  cd $temp && mvn clean install 
done
