#!/bin/bash
cd ../../openapi-directory
curr=`pwd`
find . -name "openapi.yaml"|sort|while read f; do
  cd "$curr"
  codegenc "$f"
done
