# Startup (sviluppo)
Stack Portainer `kodama-api-dev` da `docker-compose.dev.yml`.
- API: http://localhost:8081 — DB: localhost:5434 — Debug: localhost:5005
- Log: Portainer → Containers → kodama-api-dev → Logs
## Docker
`docker compose up -d`
## Spring Boot
`./mvnw spring-boot:run`
## Ngrok
`ngrok http 8081`