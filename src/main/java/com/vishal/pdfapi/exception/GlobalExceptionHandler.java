package com.vishal.pdfapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Standardized API Error Response for all 4xx/5xx status codes
    private record ApiErrorResponse(String requestId, Instant timestamp, int status, String error, String message) {}

    // --- 400 Bad Request Handlers ---

    /**
     * Handles InvalidFileException (empty, wrong type, file validation errors from Service)
     * and InvalidPasswordException. Both return 400.
     */
    @ExceptionHandler({InvalidFileException.class, InvalidPasswordException.class})
    public ResponseEntity<ApiErrorResponse> handleClientValidationExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Client Error (400): {}", e.getMessage());
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                UUID.randomUUID().toString(), Instant.now(), status.value(), status.getReasonPhrase(),
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles MissingServletRequestPartException (client did not include the 'file' part). Returns 400.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPartException(MissingServletRequestPartException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Client Error (400): Missing required part. {}", ex.getMessage());
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                UUID.randomUUID().toString(), Instant.now(), status.value(), status.getReasonPhrase(),
                "Missing required file part. Ensure the request includes a 'file' parameter."
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles general MultipartException (malformed headers, missing boundary). Returns 400.
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipartException(MultipartException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Client Error (400): Malformed multipart request. {}", ex.getMessage());

        String clientMessage = "The request body is structurally invalid (e.g., missing boundary or malformed data). Please ensure Content-Type is correct.";

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                UUID.randomUUID().toString(), Instant.now(), status.value(), status.getReasonPhrase(),
                clientMessage
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // --- 413 Payload Too Large Handler ---

    /**
     * Handles MaxUploadSizeExceededException (file larger than spring.servlet.multipart.max-file-size).
     * Returns 413 Payload Too Large.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex, WebRequest request) {
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE; // HTTP 413
        log.warn("Client Error (413): File size limit exceeded. Max: {}", ex.getMaxUploadSize());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                UUID.randomUUID().toString(), Instant.now(), status.value(), status.getReasonPhrase(),
                "File size exceeds the maximum limit of " + (ex.getMaxUploadSize() / (1024 * 1024)) + "MB."
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // --- 500 Internal Server Error Handler ---

    /**
     * Handles IOException (Corrupt PDF structure or internal I/O failure from the service). Returns 500.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handlePdfProcessingException(IOException ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        // Log stack trace for debugging internal server errors
        log.error("Server Error (500): PDF processing failed.", ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                UUID.randomUUID().toString(), Instant.now(), status.value(), status.getReasonPhrase(),
                "An internal error occurred during PDF processing. The document may be corrupt."
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}