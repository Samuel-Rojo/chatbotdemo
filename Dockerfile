FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

#copiar los archivos en maven wrapper
COPY .mvn .mvn
COPY mvnw .
COPY ["mvnw.cmd", "."]
COPY pom.xml .

#hacer mvnw ejecutable
RUN chmod +x ./mvnw

#copiar codigo fuente
COPY src ./src

#compilar aplicacion
RUN ./mvnw clean package -DskipTests -B

#listar los archivos para debugging
RUN ls -la target/

#exponer puerto
EXPOSE 8080

#buscar y ejecutar el jar generado (mas robusto)
CMD ["sh", "-c", "java -jar target/*.jar"]