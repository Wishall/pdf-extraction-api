package com.vishal.pdfapi.service;

import com.vishal.pdfapi.exception.InvalidFileException;
import com.vishal.pdfapi.exception.InvalidPasswordException;
import com.vishal.pdfapi.model.ExtractResponse;
import com.vishal.pdfapi.model.PageText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfExtractService {

  private static final Logger log = LoggerFactory.getLogger(PdfExtractService.class);

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() == 0) {
      throw new InvalidFileException("No file uploaded or file is empty.");
    }

    // Simplified check, assuming you will enforce the PDF MIME type in a future iteration
    if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
      throw new InvalidFileException("Invalid file type. Only PDF files are allowed.");
    }
  }

  public ExtractResponse extract(MultipartFile file) throws IOException {
    validateFile(file); // 1. Moved validation here

    log.info("Starting PDF text extraction. Filename='{}', size={} bytes",
            file.getOriginalFilename(), file.getSize());

    long startTime = System.currentTimeMillis();

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected/encrypted and not supported.");
      }

      int totalPages = doc.getNumberOfPages();
      PDFTextStripper stripper = new PDFTextStripper();
//      stripper.seten(StandardCharsets.UTF_8.name()); // Ensure UTF-8

      // 1. Extract full text
      String fullText = stripper.getText(doc).trim();

      // 2. Extract per-page text
      List<PageText> pages = new ArrayList<>();
      for (int i = 1; i <= totalPages; i++) {
        stripper.setStartPage(i);
        stripper.setEndPage(i);
        String pageText = stripper.getText(doc).trim();

        pages.add(new PageText(i, pageText));
      }

      long elapsed = System.currentTimeMillis() - startTime;
      log.info("PDF extraction completed in {} ms. Total pages: {}", elapsed, totalPages);

      // Return immutable record
      return new ExtractResponse(fullText, pages, totalPages);

    } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
      // Catches encrypted files even if isEncrypted() failed
      throw new InvalidPasswordException("PDF is password-protected/encrypted and not supported.");
    } catch (IOException ex) {
      // Catches structural corruption errors (mapped to 500)
      log.error("PDF extraction failed for '{}': File corruption or structural error.", file.getOriginalFilename(), ex);
      // 💡 FIX: Inspect the message to check for common corruption signatures
      String msg = ex.getMessage().toLowerCase();

      if (msg.contains("end-of-file") || msg.contains("stream") || msg.contains("invalid") || msg.contains("corrupt")) {
        // Re-throw as a client error (400)
        throw new InvalidFileException("The uploaded PDF document appears to be corrupt or malformed.");
      }
      throw ex;
    }
  }

  public Map<String, Object> extractMetadata(MultipartFile file) throws IOException {
    validateFile(file); // 1. Moved validation here

    log.info("Starting metadata extraction for '{}'", file.getOriginalFilename());

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected/encrypted. Metadata cannot be extracted.");
      }

      Map<String, Object> map = new HashMap<>();

      map.put("pages", doc.getNumberOfPages());
      map.put("encrypted", doc.isEncrypted());
      map.put("version", doc.getVersion());

      // ... (Metadata fields remain the same) ...
      if (doc.getDocumentInformation() != null) {
        PDDocumentInformation info = doc.getDocumentInformation();
        map.put("title", info.getTitle());
        map.put("author", info.getAuthor());
        map.put("subject", info.getSubject());
        map.put("keywords", info.getKeywords());
        map.put("creator", info.getCreator());
        map.put("producer", info.getProducer());
        map.put("creationDate", info.getCreationDate() != null ? info.getCreationDate().getTime() : null);
        map.put("modificationDate", info.getModificationDate() != null ? info.getModificationDate().getTime() : null);
      }

      log.info("Metadata extraction complete for '{}'", file.getOriginalFilename());
      return map;

    } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
      throw new InvalidPasswordException("PDF is password-protected/encrypted. Metadata cannot be extracted.");
    } catch (IOException ex) {
      log.error("Metadata extraction failed for '{}': Structural error.", file.getOriginalFilename(), ex);
      throw ex;
    }
  }
}