FROM openjdk:11
COPY rodi-forwarder rodi-forwarder
ENV RODIFORWARD=""
ENTRYPOINT ["rodi-forwarder/bin/rodi-forwarder"]