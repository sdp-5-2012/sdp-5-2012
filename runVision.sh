#!/bin/bash

cd bin
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:../lib
java -cp ../lib/*:. Planning.Runner
