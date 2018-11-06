#!/bin/sh

CURRENT=$(cd $(dirname $0);pwd)
SCRIPT=$CURRENT/src/main/java
CLASSPATH=/home/ubuntu/catkin_ws_java/lib/*



javac -classpath ${CLASSPATH}: -sourcepath ${SCRIPT}: ${SCRIPT}/Main.java
cd ${SCRIPT}
java -cp ${CLASSPATH}: org.ros.RosRun Main
