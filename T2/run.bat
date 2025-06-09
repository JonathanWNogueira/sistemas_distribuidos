@echo off
del /s /q *.class
javac *.java

start "Server"   cmd /k "title Server && java ServerChat"
start "Client 1" cmd /k "title Client 1 && java ClientGUI localhost"
start "Client 2" cmd /k "title Client 2 && java ClientGUI localhost"

