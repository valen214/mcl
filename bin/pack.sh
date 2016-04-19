#!/bin/sh
cd /home/ubuntu/workspace
javac -cp .:launcher.jar start/*.java && jar cfe Start.jar start.Start start/* LZMA/*