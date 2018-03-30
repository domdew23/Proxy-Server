#!/bin/bash

cd src
javac -d ../bin com/dom/dope/*.java
javac -d ../bin com/dom/util/*.java
javac -d ../bin com/dom/client/*.java
javac -d ../bin com/dom/server/*.java
cd ..

echo Built.
