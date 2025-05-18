DTB BANKING PLATFORM - TECHNICAL ASSESSMENT
-------------------------------------------

Author: Faisal Abdirashid

Overview:
---------
This project demonstrates a banking platform using a microservices architecture built with Java, Spring Boot (WebFlux), PostgreSQL, Docker, and WebClient for inter-service communication.

There are three independent services:
1. Customer Service
2. Account Service
3. Card Service

Each service is responsible for a specific domain. All communication is done via REST API. The services are containerized and connected via Docker Compose.

-------------------------------------------
MICROSERVICES DESCRIPTION
-------------------------------------------

1. Customer Service (port 8081):
   - Handles customer bio-data (firstName, lastName, otherName)
   - Filters: name (full text), created date range
   - CRUD APIs available

2. Account Service (port 8082):
   - Manages customer accounts (IBAN, BIC/SWIFT, customerId)
   - Each account is linked to a customer
   - Validates existence of customer via Customer Service (WebClient)
   - Filters: IBAN, BIC/SWIFT, Card Alias (uses Card Service for alias filter)
   - Enforces unique IBAN

3. Card Service (port 8083):
   - Manages card info (alias, PAN, CVV, accountId, type)
   - Max 2 cards per account (only one of each type)
   - Sensitive data (PAN, CVV) is masked by default unless unmasked via ?unmask=true
   - Filters: alias, PAN, card type

-------------------------------------------
TECHNOLOGIES USED
-------------------------------------------
- Java 21
- Spring Boot 3 + WebFlux
- R2DBC (Reactive PostgreSQL)
- Docker + Docker Compose
- Maven
- JUnit 5 + Mockito for unit testing
- WebClient for REST communication
- Bean Validation (javax.validation)

-------------------------------------------
RUNNING THE APPLICATION (Recommended)
-------------------------------------------

Requirements:
- Docker + Docker Compose

Steps:

1. Navigate to the root folder where `docker-compose.yml` is located

2. Run:

   docker-compose up --build

This will:
- Build all services using Maven (tests will run during build)
- Start:
   - Customer Service on port 8081
   - Account Service on port 8082
   - Card Service on port 8083
   - PostgreSQL containers for each service

 All builds include tests.

-------------------------------------------
OPTIONAL: RUN SERVICES INDEPENDENTLY
-------------------------------------------

Requirements:
- Java 21
- Maven

Steps (for each service):

1. Navigate into the service directory (e.g. `cd customer-service`)

2. Run tests:

   mvn test

3. Start the application:

   mvn spring-boot:run

Each service runs on:
- Customer: http://localhost:8081
- Account: http://localhost:8082
- Card: http://localhost:8083

-------------------------------------------
POSTMAN COLLECTION
-------------------------------------------
The Postman collection is included in the ZIP folder.

It includes:
- Create, Get, Update, Delete requests for all 3 services
- Filtering requests
- Requests with pagination
- Requests with and without PAN/CVV masking

To import:
- Open Postman
- Click "Import" and select the JSON file from the ZIP

-------------------------------------------
HOW THIS SYSTEM COULD BE IMPROVED FOR A REAL PRODUCTION BANKING SYSTEM
-------------------------------------------

1. Authentication and Authorization:
   - Add OAuth2/JWT-based security using Spring Security
   - Role-based access (e.g. ADMIN, CUSTOMER)

2. API Gateway:
   - Introduce an API Gateway (e.g., Spring Cloud Gateway) to centralize routing, auth, rate-limiting.

3. Service Registry & Discovery:
   - Use a tool like Eureka or Consul for service discovery and auto-scaling of microservices.

4. Resilience:
   - Use Circuit Breakers (e.g. Resilience4J) for WebClient calls to handle downstream failures.
   - Retry policies and fallbacks for external dependencies.

5. Distributed Tracing & Monitoring:
   - Implement OpenTelemetry or Spring Sleuth + Zipkin/Grafana for tracing and metrics.

6. Centralized Logging:
   - Use ELK stack (Elasticsearch, Logstash, Kibana) or Loki for centralized structured logging.

7. PAN/CVV Management:
   - Move PAN and CVV to a secure vault (e.g. HashiCorp Vault or HSM)
   - Never store CVV in raw form — tokenize or hash where possible.

8. Database Hardening:
   - Add encryption at rest and in transit.
   - Partition sensitive tables (card, customer) and apply access controls.

9. Asynchronous Communication:
   - Use messaging (Kafka/RabbitMQ) for decoupling services (e.g., customer account created → card service notified).

10. Validation & Auditing:
    - Introduce detailed auditing for all critical actions.
    - Add field-level validations, anti-fraud checks, and input sanitization.

11. Rate Limiting & Throttling:
    - Protect APIs from abuse using throttling and quota management.

12. CI/CD and DevOps:
    - Automate build/test/deploy pipelines using Jenkins, GitHub Actions, or GitLab CI.
    - Container scanning and vulnerability analysis.

13. Schema Evolution:
    - Use Liquibase or Flyway for version-controlled database migrations.

-------------------------------------------
Thank you for reviewing!

Faisal Abdirashid
