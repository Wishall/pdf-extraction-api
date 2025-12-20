package com.vishal.pdfapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.pdfapi.model.ExtractResponse;
import com.vishal.pdfapi.model.JsonFilePayload;
import com.vishal.pdfapi.model.PdfMetadataResponse;
import com.vishal.pdfapi.service.PdfExtractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "PDF Extraction API", description = "Endpoints for text and metadata extraction")
@RestController
@RequestMapping("/api")
public class ExtractController {

  private static final Logger log = LoggerFactory.getLogger(ExtractController.class);

  @Autowired
  private PdfExtractService service;

  @Autowired
  private Environment env;

  @Autowired
  private ObjectMapper objectMapper; // Autowire ObjectMapper for JSON logging

  @Operation(summary = "Health check endpoint", description = "Returns a simple 'UP' status if the service is running.")
  @ApiResponse(responseCode = "200", description = "Service is operational")
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> healthCheck() {
    return ResponseEntity.ok(Collections.singletonMap("status", "UP"));
  }

  @GetMapping("/debug-limits")
  public Map<String, String> debugLimits() {
    Map<String, String> map = new HashMap<>();
    map.put("activeProfile", String.join(",", env.getActiveProfiles()));
    map.put("max-file-size", env.getProperty("spring.servlet.multipart.max-file-size"));
    map.put("max-request-size", env.getProperty("spring.servlet.multipart.max-request-size"));
    return map;
  }

  @Operation(
          summary = "Extract text from a PDF file",
          description = "Returns full text + per-page text from an uploaded PDF file (multipart/form-data)."
  )
  @PostMapping(value = "/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ExtractResponse> extract(@RequestPart("file") MultipartFile file) throws IOException {
    log.info("Received /extract-text request. Filename='{}', size={} bytes",
            file != null ? file.getOriginalFilename() : "null",
            file != null ? file.getSize() : 0);
    return ResponseEntity.ok(service.extract(file));
  }

  @Operation(
          summary = "Extract text from a Base64 JSON payload",
          description = "Returns full text + per-page text from a PDF sent as a Base64 string in a JSON object."
  )
  @PostMapping(value = "/extract-text-json", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ExtractResponse> extractTextFromJson(
          @RequestBody JsonFilePayload payload
  ) throws IOException {
    log.info(">>>>>>>>>> /extract-text-json endpoint reached. Attempting to process payload. <<<<<<<<<<");
    try {
      log.info("Spring successfully deserialized payload: {}", objectMapper.writeValueAsString(payload));
    } catch (JsonProcessingException e) {
      log.warn("Could not serialize payload for logging.", e);
    }

    if (payload == null || payload.getFileContent() == null || payload.getFileContent().isEmpty()) {
        log.error("Validation failed: payload or fileContent is null or empty.");
        throw new IllegalArgumentException("fileContent in JSON payload cannot be null or empty.");
    }

    byte[] pdfBytes = Base64.getDecoder().decode(payload.getFileContent());
    log.info("Successfully decoded Base64 content. Size={} bytes", pdfBytes.length);

    MultipartFile file = new MockMultipartFile(
            "file",
            "uploaded.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            pdfBytes
    );

    ExtractResponse response = service.extract(file);
    log.info(">>>>>>>>>> Successfully processed /extract-text-json request. <<<<<<<<<<");
    return ResponseEntity.ok(response);
  }


  @Operation(
          summary = "Extract metadata from a PDF",
          description = "Returns title, author, subject, page count, and other metadata"
  )
  @PostMapping(value = "/metadata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PdfMetadataResponse> metadata(@RequestPart("file") MultipartFile file) throws IOException {
    log.info("Received /metadata request for '{}'",
            file != null ? file.getOriginalFilename() : "null");
    Map<String, Object> data = service.extractMetadata(file);
    return ResponseEntity.ok(new PdfMetadataResponse(data));
  }
}
