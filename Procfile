release: ./mvnw clean install -DskipTests -pl axsalud-migrations flyway:migrate
web: java -Dserver.port=$PORT $JAVA_OPTS -jar axsalud-web-app/target/*.jar
