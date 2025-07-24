FROM busybox

ARG APP_VERSION
ENV APP_VERSION=${APP_VERSION}

COPY ./target/event-listener-${APP_VERSION}.jar /event-listener.jar
