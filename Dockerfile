FROM gcr.io/distroless/java21-debian12:nonroot
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./target/bidrag-dokument-bestilling-*.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
ENV TZ="Europe/Oslo"
ENV SPRING_PROFILES_ACTIVE=nais

CMD ["app.jar"]