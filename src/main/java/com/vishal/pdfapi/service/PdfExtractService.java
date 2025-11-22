package com.vishal.pdfapi.service;

import com.vishal.pdfapi.model.ExtractResponse;
import com.vishal.pdfapi.model.PageText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfExtractService {

  private static final Logger log = LoggerFactory.getLogger(PdfExtractService.class);

  public ExtractResponse extract(MultipartFile file) throws IOException {

    log.info("Starting PDF extraction. File='{}', size={} bytes",
            file.getOriginalFilename(),
            file.getSize());

    long startTime = System.currentTimeMillis();

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected. Not supported.");
      }

      int totalPages = doc.getNumberOfPages();
      log.info("PDF loaded successfully. Total pages={}", totalPages);

      PDFTextStripper stripper = new PDFTextStripper();

      // Extract full text
      String fullText = stripper.getText(doc);
      log.debug("Full text extracted ({} chars)", fullText.length());

      ExtractResponse response = new ExtractResponse();
      response.success = true;
      response.text = fullText;

      // Extract per-page text
      List<PageText> pages = new ArrayList<>();
      for (int i = 1; i <= totalPages; i++) {
        stripper.setStartPage(i);
        stripper.setEndPage(i);
        String pageText = stripper.getText(doc);

        pages.add(new PageText(i, pageText));
        log.debug("Extracted page {} ({} chars)", i, pageText.length());
      }

      response.pages = pages;

      long elapsed = System.currentTimeMillis() - startTime;
      log.info("PDF extraction completed successfully in {} ms", elapsed);

      return response;

    } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
      throw new InvalidPasswordException("PDF is password-protected. Not supported.");
    } catch (IOException ex) {
      log.error("PDF extraction failed for '{}': {}", file.getOriginalFilename(), ex.getMessage(), ex);
      throw ex; // propagate to controller
    }
  }

  public Map<String, Object> extractMetadata(MultipartFile file) throws IOException {

    log.info("Starting metadata extraction for '{}'", file.getOriginalFilename());

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected. Metadata cannot be extracted.");
      }

      Map<String, Object> map = new HashMap<>();

      map.put("pages", doc.getNumberOfPages());
      map.put("encrypted", doc.isEncrypted());
      map.put("version", doc.getVersion());

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
      throw new InvalidPasswordException("PDF is password-protected. Metadata cannot be extracted.");
    } catch (IOException ex) {
      log.error("Metadata extraction failed for '{}': {}", file.getOriginalFilename(), ex.getMessage());
      throw ex;
    }
  }

  public static class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
      super(message);
    }
  }
}
