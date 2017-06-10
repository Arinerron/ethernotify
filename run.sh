#!/bin/sh

set -e

rm -rf bin > /dev/null 2>&1

mkdir bin

cd src
javac -cp .:../lib/* -d ../bin/ Main.java
cd ../bin
java -cp .:../lib/* Main $@
cd ..
