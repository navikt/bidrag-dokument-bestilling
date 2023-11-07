FROM busybox:1.36.1-uclibc as busybox

FROM gcr.io/distroless/java21-debian12:nonroot
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv

COPY ./target/bidrag-dokument-bestilling-*.jar /app/app.jar

WORKDIR /app
EXPOSE 8080
ENV TZ="Europe/Oslo"
ENV SPRING_PROFILES_ACTIVE=nais
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
CMD ["app.jar"]