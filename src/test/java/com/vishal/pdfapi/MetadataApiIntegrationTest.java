package com.vishal.pdfapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.vishal.pdfapi.TestFileUtil.load;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class MetadataApiIntegrationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    // ----------------------------
    // 1. VALID PDF → SUCCESS
    // ----------------------------
    @Test
    void testMetadataValidPdf() {
        given()
                .multiPart("file", "valid.pdf", load("valid.pdf"))
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(200)
                .body("metadata.pages", greaterThanOrEqualTo(1))
                .body("metadata.encrypted", equalTo(false))
                .body("metadata", hasKey("creator"));
    }

    // ----------------------------
    // 2. WRONG FILE TYPE
    // ----------------------------
    @Test
    void testMetadataWrongFileType() {
        given()
                .multiPart("file", "ganesha.png", load("ganesha.png"))
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("Only PDF files"));
    }

    // ----------------------------
    // 3. EMPTY FILE
    // ----------------------------
    @Test
    void testMetadataEmptyFile() {
        given()
                .multiPart("file", "empty.pdf", new byte[0])
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("No file"));
    }

    // ----------------------------
    // 4. PASSWORD PROTECTED → 400
    // ----------------------------
    @Test
    void testMetadataPasswordProtectedPdf() {
        given()
                .multiPart("file", "locked.pdf", load("locked.pdf"))
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("password-protected"));
    }

    // ----------------------------
    // 5. LARGE FILE → REJECTED
    //      (based on application-test.yml limit)
    // ----------------------------
    @Test
    void testMetadataLargeFileRejected() {
        given()
                .multiPart("file", "large.pdf", load("large.pdf"))
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(413)
                .body("message", containsStringIgnoringCase("File size exceeds"));
    }

    // ----------------------------
    // 6. CORRUPT PDF → INTERNAL ERROR
    // ----------------------------
    @Test
    void testMetadataCorruptPdf() {
        given()
                .multiPart("file", "corrupt.pdf", load("corrupt.pdf"))
                .when()
                .post("/api/metadata")
                .then()
                .statusCode(500);
    }
}
