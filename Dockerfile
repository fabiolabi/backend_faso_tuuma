# ============================================================
#  Faso Tuuma — image Docker (multi-stage)
#  Stage 1 : build du jar avec Maven (JDK 21)
#  Stage 2 : runtime léger (JRE 21), non-root
#  Les ${VAR} des application*.properties sont résolus depuis
#  l'environnement du conteneur AU LANCEMENT (voir docker-compose.yml).
# ============================================================

# ---- Stage build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Couche dépendances en cache : ne se recalcule que si pom.xml change.
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Code source puis build du jar (tests ignorés : exécutés hors image).
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ---- Stage runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Utilisateur non-root.
RUN useradd --system --uid 1001 --shell /usr/sbin/nologin appuser

# Jar applicatif (un seul *.jar produit, hors *-sources/-javadoc).
COPY --from=build /app/target/*.jar /app/app.jar

# Répertoire de stockage media (FS local) — monté en volume en prod.
RUN mkdir -p /app/data/media && chown -R appuser:appuser /app

USER appuser

# Profil prod par défaut (surchargé par SPRING_PROFILES_ACTIVE si fourni).
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xms512m", "-Xmx1024m", "-XX:MaxGCPauseMillis=200", "-jar", "/app/app.jar"]
