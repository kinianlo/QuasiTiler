#! /bin/sh
javac 2>&1 -Xlint:deprecation -classpath classes -sourcepath src -d classes src/Quasitiler/*.java | tee .mk.log
