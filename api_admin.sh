#!/bin/sh
SERVICE_NAME=ResmatAPI
PATH_TO_JAR="resmat-assembly-1.0.jar"
PATH_TO_RESOURCES="."
PID_PATH_NAME=ResmatAPI-pid

MAIN_CLASS=edu.knuca.resmat.Main

case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -cp ${PATH_TO_RESOURCES}:${PATH_TO_JAR} ${MAIN_CLASS} > log.txt 2> errors.txt < /dev/null &
            echo $! > ${PID_PATH_NAME}
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java -cp ${PATH_TO_RESOURCES}:${PATH_TO_JAR} ${MAIN_CLASS} > log.txt 2> errors.txt < /dev/null &
            echo $! > ${PID_PATH_NAME}
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac