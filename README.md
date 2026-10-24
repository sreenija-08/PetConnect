# PetConnect 🐾 - Pet Adoption and Shelter Management System

An enterprise-grade, end-to-end pet adoption platform built with **Spring Boot 3**, **MySQL**, and an **AI-powered Python FastAPI Microservice** for automated shelter verification.

---

## 🌟 Key Features

- **Pet Adoption Marketplace**: Browse adoptable pets with rich details (breed, age, health status, photos), search and filter.
- **Shelter Onboarding & Management**: Shelters can register, manage pet profiles, review adoption applications, and communicate with adopters.
- **AI Document Verification Microservice**: Automated verification of shelter registration certificates and legal documents using computer vision, OCR, and document authenticity scoring.
- **Adoption Application Workflow**: Complete lifecycle tracking from application submission to shelter review, interview, and approval.
- **Admin Oversight Dashboard**: High-level system monitoring, shelter approval management, and platform analytics.
- **Role-Based Access Control**: Granular security rules for Adopters, Shelters, and Administrators.

---

## 🛠️ Architecture & Tech Stack

- **Backend**: Java 17+, Spring Boot 3.3.3 (Spring MVC, Spring Data JPA, Spring Security)
- **Database**: MySQL 8.0 with Hibernate ORM
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript, TailwindCSS / Bootstrap
- **AI Microservice**: Python 3.10+, FastAPI, Uvicorn, OCR & Document Processing
- **Build Tool**: Apache Maven

```
                   +------------------------+
                   |     Web Browser /      |
                   |   Thymeleaf Frontend   |
                   +-----------+------------+
                               |
                        HTTP / REST
                               |
                   +-----------v------------+
                   |  Spring Boot 3 App     |
                   |      (Port 8080)       |
                   +------+-----------+-----+
                          |           |
            JPA / JDBC    |           | REST API Call
                          |           | (Port 8000)
             +------------v-+       +-v----------------------+
             | MySQL 8.0 DB |       | Python FastAPI Service |
             | (Port 3306)  |       | (AI Doc Verification)  |
             +--------------+       +------------------------+
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or 21
- **Python**: Version 3.10 or higher
- **MySQL Server**: Version 8.0
- **Maven**: 3.8+ (or bundled wrapper)

---

### 1. Database Setup

Create the MySQL database:

```sql
CREATE DATABASE petconnect;
```

Configure your credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/petconnect?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

---

### 2. Run the AI Microservice

In a separate terminal:

```bash
cd ai_service
pip install fastapi uvicorn pydantic python-multipart
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

The AI verification service will run on `http://localhost:8000`.

---

### 3. Run the Spring Boot Application

In the project root directory:

**Using Maven:**
```bash
mvn spring-boot:run
```

**Using the included runner script (Windows):**
```cmd
run.bat
```

The main application will be available at `http://localhost:8080/`.

---

## 📁 Project Structure

```
petconnect/
├── ai_service/                      # Python FastAPI AI document verification service
│   ├── main.py                      # FastAPI server endpoints
│   ├── verifier.py                  # Certificate validation & scoring logic
│   └── generate_demo_certificates.py
├── src/
│   ├── main/
│   │   ├── java/com/petconnect/     # Spring Boot controllers, models, services, repositories
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf views (auth, adopter, shelter, admin)
│   │       └── application.properties
├── uploads/                         # User-uploaded pet photos and shelter docs
├── PetConnect_Project_Documentation.pdf   # Complete Project Report
├── pom.xml                          # Maven build dependencies
├── run.bat                          # Quick start script for Windows
└── README.md
```

---

## 📄 Documentation

A full 39-page comprehensive project report including architectural diagrams, ER diagrams, data dictionaries, test suites, and output screens is available in `PetConnect_Project_Documentation.pdf`.

---

## 👥 Authors & Contributors

- **Sreenija** ([@sreenija-08](https://github.com/sreenija-08))
