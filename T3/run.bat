@echo off
del /s /q *.class
javac *.java
start "Client 5000" cmd /k "title Client 5000 && java Client localhost 5000"
start "Client 5001" cmd /k "title Client 5001 && java Client localhost 5001"
start "Client 5002" cmd /k "title Client 5002 && java Client localhost 5002"