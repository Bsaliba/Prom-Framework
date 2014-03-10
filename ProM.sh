#!/bin/sh

###
## ProM specific
###
PROGRAM=ProM63
CP=./${PROGRAM}.jar
LIBDIR=./lib
MAIN=org.processmining.contexts.uitopia.UI

####
## Environment options
###
JAVA=/usr/java/default/bin/java
MEM=1g

###
## Main program
###

add() {
	CP=${CP}:$1
}


for lib in $LIBDIR/*.jar
do
	add $lib
done

$JAVA -classpath ${CP} -Djava.library.path=${LIBDIR} -ea -Xmx${MEM} -XX:+UseCompressedOops ${MAIN}
