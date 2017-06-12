#!/bin/sh  
  
WORK_DIR=$(cd `dirname $0`; pwd)/../
LOG_CONF=file:${WORK_DIR}/conf/logback.xml
LOG_DIR=/services/logs/Frequency controller

if [ ! -d "$LOG_DIR" ] ; then
   mkdir "$LOG_DIR"
fi

PID_FILE=/var/run/Frequency controller.pid

JAVA=/usr/bin/java
JAVA_OPTS="-server -Xms1024m -Xmx1024m -Xmn384m -XX:+HeapDumpOnOutOfMemoryError
 -XX:HeapDumpPath=/services/logs/Frequency controller/oom.hprof -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
 -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ExplicitGCInvokesConcurrent
 -XX:-UseBiasedLocking -XX:+AlwaysPreTouch -XX:+CMSParallelRemarkEnabled -XX:AutoBoxCacheMax=20000
 -Dwork.dir=${WORK_DIR}
 -Dcom.sun.management.jmxremote.port=8555
 -Dcom.sun.management.jmxremote.ssl=false
 -Dcom.sun.management.jmxremote.authenticate=false
 -Dlogger.file=${LOG_CONF} -Dfile.encoding=UTF-8 -Duser.timezone=UTC"
CLASS_PATH=" -classpath ":$(echo ${WORK_DIR}/lib/*.jar|sed 's/ /:/g')
CLASS_PATH=${CLASS_PATH}:${WORK_DIR}/conf/
CLASS=com.maxent.Boot

cd $WORK_DIR
  
case "$1" in  
  
  start)
  	if [ -f "${PID_FILE}" ]; then
    	echo "Frequency controller is running,pid=`cat ${PID_FILE}`."
    else
    	exec "$JAVA" $JAVA_OPTS $CLASS_PATH $CLASS >> ${LOG_DIR}/startup.log 2>&1 &
		echo "Frequency controller is running,pid=$!."
    	echo $! > ${PID_FILE}
    fi
    ;;  
  
  stop)  
  	if [ -f "${PID_FILE}" ]; then
    	kill -9 `cat ${PID_FILE}`  
    	rm -rf ${PID_FILE}  
    	echo "Frequency controller is stopped."
    else
    	echo "Frequency controller is not running."
    fi
    ;;  
  
  restart)  
    $0 stop
    sleep 1  
    $0 start
    ;;  

  status)
  	if [ -f "${PID_FILE}" ]; then
    	echo "Frequency controller is running,pid=`cat ${PID_FILE}`."
    else
    	echo "Frequency controller is not running."
    fi
    ;;
    
  *)  
    echo "Usage: Frequency controller.sh {start|stop|restart|status}"
    ;;  
  
esac
  
exit 0