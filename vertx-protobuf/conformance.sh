#!/bin/bash
#exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -jar target/vertx-protobuf-5.1.0-SNAPSHOT-conformance.jar "$@"
exec java -jar target/vertx-protobuf-5.1.0-SNAPSHOT-conformance.jar "$@"
