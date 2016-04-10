#!/bin/sh
javac -cp .:launcher.jar start/*.java && jar cfe Start.jar start.Start start/* LZMA/*