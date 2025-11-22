package com.vishal.pdfapi.controller;

import com.vishal.pdfapi.model.ExtractResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

  @GetMapping("/debug-limits")
  public Map<String, String> debugLimits() {
    Map<String, String> map = new HashMap<>();
    map.put("activeProfile", String.join(",", env.getActiveProfiles()));
    map.put("max-file-size", env.getProperty("spring.servlet.multipart.max-file-size"));
    map.put("max-request-size", env.getProperty("spring.servlet.multipart.max-request-size"));
    return map;
  }

  @Operation(
          summary = "Extract text from a PDF",
          description = "Returns full text + per-page text from uploaded PDF"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Extraction successful",
                  content = @Content(schema = @Schema(implementation = ExtractResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/extract-text")
  public ResponseEntity<?> extract(@RequestPart("file") MultipartFile file) throws IOException {
    log.info("Received extract-text request. Filename='{}', size={} bytes",
            file != null ? file.getOriginalFilename() : "null",
            file != null ? file.getSize() : 0);

    // 1. Empty file check
    if (file == null || file.isEmpty()) {
      log.warn("Rejected request: empty or null file.");
      return ResponseEntity.badRequest()
              .body(new ExtractResponse(false, "No file uploaded or Empty file"));
    }

    // 2. File type check
    if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
      log.warn("Rejected request: invalid file type '{}'", file.getOriginalFilename());
      return ResponseEntity.badRequest()
              .body(new ExtractResponse(false, "Only PDF files are allowed"));
    }

    log.info("File validated. Forwarding to PdfExtractService for extraction.");

    return ResponseEntity.ok(service.extract(file));
  }

  @Operation(
          summary = "Extract metadata from a PDF",
          description = "Returns title, author, subject, page count, and other metadata"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Metadata extracted"),
          @ApiResponse(responseCode = "400", description = "Invalid input"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/metadata")
  public ResponseEntity<?> metadata(@RequestPart("file") MultipartFile file) throws IOException {
    log.info("Received metadata request for '{}'",
            file != null ? file.getOriginalFilename() : "null");

    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest()
              .body(new PdfMetadataResponse(false, "No file uploaded or Empty file"));
    }

    if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
      return ResponseEntity.badRequest()
              .body(new PdfMetadataResponse(false, "Only PDF files are allowed"));
    }

    Map<String, Object> data = service.extractMetadata(file);
    return ResponseEntity.ok(new PdfMetadataResponse(true, data));
  }

}
