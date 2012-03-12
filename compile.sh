#!/bin/bash

find -name "*.java" > sources.txt

javac -cp ./lib/*:. -d ./bin @sources.txt
