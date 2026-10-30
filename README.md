# PerfectHousehold Backend

Backend uwierzytelniania napisany w **Java 21** i **Spring Boot 3**, wykorzystujący **MyBatis** (z zapytaniami SQL w XML i interfejsami DAO), **PostgreSQL**, **Flyway**, architekturę **GenericService** (bezpośrednie mapowanie JSON -> model domenowy bez narzutu klas DTO) oraz **Spring Security / OAuth2** z obsługą logowania biometrycznego (**Passkeys / WebAuthn / FIDO2**).

---

## 🚀 Główne Cechy

1. **GenericService & mapDbObject (Bez DTO)**
   - Bazowa klasa `GenericService` z metodą `mapDbObject(Object source, Class<T> targetClass)` przyjmuje wejściowy JSON (String, JsonNode, Map) i bezpośrednio tworzy obiekt encji (`User.class`) bez definiowania zbędnych klas DTO.
   - Brak niepotrzebnego narzutu domyślnych metod CRUD po ID w serwisie.

2. **Warstwa Persystencji (MyBatis + DAO w XML)**
   - Ręcznie pisane, zoptymalizowane zapytania SQL w XML (`src/main/resources/mapper/`).
   - Bezpośrednie interfejsy DAO (`UserDao`, `BiometricCredentialDao`) bez abstrakcji Hibernate.

3. **Autoryzacja, OAuth2 & Biometria**
   - API zabezpieczone tokenami JWT.
   - Dedykowany kontroler `AuthController` oraz serwisy autoryzacyjne (`AuthService`, `UserService`, `BiometricAuthService`).
   - Endpointy:
     - `POST /api/auth/register` (bezpośrednia rejestracja z JSON bez DTO)
     - `POST /api/auth/login` (logowanie hasłem, zwrot JWT)
     - `POST /api/auth/oauth2/callback` (OAuth2 callback)
     - `POST /api/auth/biometric/challenge` (generowanie challenge dla WebAuthn / Passkeys)
     - `POST /api/auth/biometric/authenticate` (weryfikacja asercji biometrycznej i wydanie JWT)
     - `POST /api/auth/biometric/register` (rejestracja klucza publicznego biometrii)

4. **Baza Danych i Migracje (PostgreSQL + Flyway)**
   - Migracja w `src/main/resources/db/migration/V1__init_schema.sql` (tabele `users` oraz `biometric_credentials`).
   - Konfiguracja `docker-compose.yml` dla PostgreSQL.

---

## 📁 Struktura Projektu

```text
perfectHousehold-backend/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/perfecthousehold/
    │   │   ├── common/
    │   │   │   ├── exception/MappingException.java
    │   │   │   └── service/GenericService.java       <-- mapDbObject (bez DTO)
    │   │   ├── config/
    │   │   │   ├── JacksonConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   └── AuthController.java               <-- dedykowany kontroler auth
    │   │   ├── dao/
    │   │   │   ├── UserDao.java                      <-- DAO MyBatis
    │   │   │   └── BiometricCredentialDao.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   └── BiometricCredential.java
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── UserPrincipal.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   └── biometric/BiometricAuthService.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java                  <-- serwis auth
    │   │   │   └── UserService.java                  <-- serwis użytkowników (GenericService)
    │   │   └── PerfectHouseholdApplication.java
    │   └── resources/
    │       ├── application.yml
    │       ├── db/migration/
    │       │   └── V1__init_schema.sql
    │       └── mapper/
    │           ├── UserDao.xml                       <-- SQL XML
    │           └── BiometricCredentialDao.xml
    └── test/
        └── java/com/perfecthousehold/common/service/
            └── GenericServiceTest.java
```
