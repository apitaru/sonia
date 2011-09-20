@echo off

set CLASSPATH=%CLASSPATH%;JSynClasses.jar;core.jar;

C:\jdk1.5.0_03\bin\javac -source 1.3 -target 1.1 *.java -d .
C:\jdk1.5.0_03\bin\jar -cf sonia_v29b.jar .\pitaru


pause;

