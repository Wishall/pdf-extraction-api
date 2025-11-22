📄 PDF Extraction API

A lightweight, production-ready PDF text & metadata extraction service.



🚀 Overview

PDF Extraction API is a fast, secure, and developer-friendly service for:

Extracting full text from PDFs

Extracting per-page text

Reading PDF metadata (author, title, creation date, etc.)

Automatically handling password-protected PDFs (returns a clear 400 error)

Gracefully rejecting invalid PDFs

Enforcing upload size limits via Spring configuration

This project is designed as an MVP intended for RapidAPI monetization, cloud deployment (OCI/AWS), or internal use.


✨ Features

✔ Extract complete text from any PDF
✔ Extract text page-by-page
✔ Extract metadata (title, author, keywords, producer)
✔ Detect password-protected PDFs
✔ Proper validation & error messages
✔ Centralized Global Exception Handler
✔ Full integration test suite using Rest Assured + JUnit
✔ Dockerfile included
✔ OpenAPI 3 generated automatically at /v3/api-docs


📦 API Endpoints
POST /api/extract-text

Uploads a PDF and returns extracted text.

Response fields:

success — boolean

text — full extracted text

pages[] — list of { pageNumber, text }

POST /api/extract-metadata

Returns metadata from the uploaded PDF.

Response fields:

success

metadata — map of extracted metadata fields

message (only on failure)

OpenAPI Spec

JSON: http://localhost:8080/v3/api-docs

Swagger UI (optional): http://localhost:8080/swagger-ui.html (if UI dependency added)

🛠️ Tech Stack

Java 21+

Spring Boot 3.4

Apache PDFBox

Rest Assured (integration tests)

Docker-ready build

🧪 Running Tests
mvn clean test


Includes:

Valid PDF extraction

Wrong file type

Empty file

Large file rejection (based on test profile)

Password-protected PDF

Corrupt PDF

Metadata extraction tests

▶️ Running Locally
mvn spring-boot:run

🐳 Running via Docker

Build:

docker build -t pdf-extraction-api .


Run:

docker run -p 8080:8080 pdf-extraction-api

⚙️ Configuration

File size limits are controlled via application.yml:

spring:
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 25MB


Tests use application-test.yml with lower limits.

🌐 Deployment Ready (OCI/AWS)

This API is suitable for:

OCI Functions / OCI Container Instances

AWS Lambda via container

AWS ECS / Fargate

Any Kubernetes cluster

Zero code changes needed.


📜 License

MIT License (optional — you can choose later)

🤝 Contributing

PRs and suggestions are welcome.

⭐ Support

If this project helps you, consider giving the repo a ★ star — it helps a lot!
