#!/bin/bash

# Create folders if they do not exist
mkdir -p classes
mkdir -p jar

javac -d classes -sourcepath MKAgent MKAgent/Main.java
jar cf jar/Agent51.jar -C classes/ .
