@echo off
set MAVEN_OPTS=-Djava.net.preferIPv4Stack=true
"%~dp0maven\apache-maven-3.9.9\bin\mvn.cmd" %*
