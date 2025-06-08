@echo off
del /s /q *.class
javac *.java

start "Client 1" cmd /k "title Client 1 && java ChatClient localhost"
start "Client 2" cmd /k "title Client 2 && java ChatClient localhost"
start "Server"   cmd /k "title Server && java ChatServer localhost"
