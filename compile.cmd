echo off
javac 2> errors -Xlint:unchecked -classpath classes -sourcepath src -d classes src\Quasitiler\*.java
type errors
