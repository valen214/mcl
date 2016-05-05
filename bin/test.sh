#!/bin/sh
cd /home/ubuntu/workspace
javac -cp . test/*.java start/*.java && jar cfe Test.jar test.Test start/* test/*