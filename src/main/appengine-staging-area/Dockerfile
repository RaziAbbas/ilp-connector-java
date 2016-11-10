FROM gcr.io/google_appengine/openjdk8

# We added a VOLUME pointing to "/tmp" because that is where a Spring Boot application creates working directories for
# Tomcat by default. The effect is to create a temporary file on your host under "/var/lib/docker" and link it to the
# container under "/tmp". This step is optional for the simple app that we wrote here, but can be necessary for other
# Spring Boot applications if they need to actually write in the filesystem.
VOLUME /tmp
VOLUME /var/log/app_engine

ADD ${artifactId}-${version}-jar-with-dependencies.jar app.jar

#You can use a RUN command to "touch" the jar file so that it has a file modification time (Docker creates all container
# files in an "unmodified" state by default). This actually isn’t important for the simple app that we wrote, but any
# static content (e.g. "index.html") would require the file to have a modification time.
RUN sh -c 'touch /app.jar'

# To reduce Tomcat startup time we added a system property pointing to "/dev/urandom" as a source of entropy.
CMD [ "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]