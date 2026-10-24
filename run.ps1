$env:MAVEN_OPTS = "-Djava.net.preferIPv4Stack=true"
& "$PSScriptRoot\maven\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
