package com.vishal.pdfapi.exception;

import com.vishal.pdfapi.controller.ExtractController;
import com.vishal.pdfapi.model.ExtractResponse;
import com.vishal.pdfapi.service.PdfExtractService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice()
public class GlobalExceptionHandler {

    @ExceptionHandler(PdfExtractService.InvalidPasswordException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordError(PdfExtractService.InvalidPasswordException e) {

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", e.getMessage());
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException ex,
                                                             HttpServletRequest request) {
        // Don't override OpenAPI internal exceptions
//        if (isOpenApiRequest(request)) throw ex;

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "File size exceeds the maximum limit");
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // Don't override OpenAPI internal exceptions
//        if (isOpenApiRequest(request)) return null;

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Internal error");
        return ResponseEntity.status(500).body(body);
    }

//    private boolean isOpenApiRequest(HttpServletRequest request) {
//        if (request == null) return false;
//        String path = request.getRequestURI();
//        return path.startsWith("/v3/");
//    }
}

