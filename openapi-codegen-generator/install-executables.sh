#!/bin/bash
set -e
set -x
sudo rm -f /usr/local/bin/codegen /usr/local/bin/codegenc
sudo ln -s `pwd`/codegen /usr/local/bin/codegen
sudo ln -s `pwd`/codegenc /usr/local/bin/codegenc
