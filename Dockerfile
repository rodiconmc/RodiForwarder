FROM openjdk:11
COPY rodi-forwarder rodi-forwarder
ENTRYPOINT ["rodi-forwarder/bin/rodi-forwarder"]