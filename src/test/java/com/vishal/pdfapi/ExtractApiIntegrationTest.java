package com.vishal.pdfapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class ExtractApiIntegrationTest {

    @LocalServerPort
    int port;

    private InputStream load(String name) {
        return getClass().getResourceAsStream("/pdfs/" + name);
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost"; // Boot test server
        RestAssured.port = port;
    }

    // 1. Valid PDF extraction with Word Count and Language Detection
    @Test
    void testValidPdfExtraction() {
        given()
                .multiPart("file", "valid.pdf", load("valid.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(200)
                .body("fullText", not(isEmptyOrNullString()))
                .body("pages", not(empty()))
                .body("pages.size()", equalTo(1))
                // New Assertions
                .body("wordCount", greaterThan(0))
                .body("language", not(isEmptyOrNullString()))
                .body("pages[0].wordCount", greaterThan(0));
    }

    // 2. Multi-page PDF extraction
    @Test
    void testMultiPagePdf() {
        given()
                .multiPart("file", "multipage.pdf", load("multipage.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(200)
                .body("fullText", not(isEmptyOrNullString()))
                .body("pages.size()", greaterThan(1))
                .body("wordCount", greaterThan(0))
                .body("language", not(isEmptyOrNullString()));
    }

    // 3. Wrong file type
    @Test
    void testWrongFileType() {
        given()
                .multiPart("file", "ganesha.png", load("ganesha.png"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Only PDF files are allowed"));
    }

    // 4. Empty file
    @Test
    void testEmptyFile() {
        given()
                .multiPart("file", "empty.pdf", new byte[0])
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("No file uploaded or file is empty"));
    }

    // 5. Large file (over limit)
    @Test
    void testLargeFileByteRejected() {
        byte[] bigFile = TestFileUtil.generateBytes(1500); // 1.5MB > 1MB limit (configured in test profile)

        given()
                .multiPart("file", "too_big.pdf", bigFile)
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(413)
                .body("message", containsStringIgnoringCase("File size exceeds the maximum limit"));
    }

    // 6. Corrupt PDF
    @Test
    void testCorruptPdf() {
        given()
                .multiPart("file", "corrupt.pdf", load("corrupt.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("corrupt or malformed"));
    }

    // 7. No file field
    @Test
    void testMissingFileField() {
        given()
                .multiPart("filew", "valid.pdf", load("valid.pdf"))
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Missing required file part"));
    }

    // 8. Incorrect file field
    @Test
    void testInvalidFileField() {
        given()
                .multiPart("file", "missing.pdf", new byte[0])
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400);
    }

    @Test
    void testSmallFileAccepted() {
        // 500KB is acceptable
        given()
                .multiPart("file", "multipage.pdf", load("multipage.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(200);
    }

    @Test
    void testLargeActualFileRejected() {
        given()
                .multiPart("file", "large.pdf", load("large.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(413)
                .body("message", containsStringIgnoringCase("File size exceeds the maximum limit"));
    }

    @Test
    void testPasswordProtectedPdfRejected() {
        given()
                .multiPart("file", "locked.pdf", load("locked.pdf"))
                .when()
                .post("/api/extract-text")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("password-protected"));
    }
}
