#Africa E-Trade Hub


##Build eth-platform to Docker daemon
 ``````
mvn compile com.google.cloud.tools:jib-maven-plugin:3.0.0:dockerBuild
 ``````
## Run multi-container applications with Docker.	
 ``````
docker-compose -p eth-stack up -d
 ``````