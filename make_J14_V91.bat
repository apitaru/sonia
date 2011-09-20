@echo off

set CLASSPATH=%CLASSPATH%;JSynClasses.jar;core.jar;

C:\j2sdk1.4.2_08\bin\javac -source 1.3 -target 1.1 *.java -d .
C:\j2sdk1.4.2_08\bin\jar -cf sonia_v29b.jar .\pitaru


pause;

