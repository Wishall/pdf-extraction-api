package com.vishal.pdfapi.service;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.vishal.pdfapi.exception.InvalidFileException;
import com.vishal.pdfapi.exception.InvalidPasswordException;
import com.vishal.pdfapi.model.ExtractResponse;
import com.vishal.pdfapi.model.PageText;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
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
  
  private LanguageDetector languageDetector;
  private TextObjectFactory textObjectFactory;

  @PostConstruct
  public void init() {
      try {
          // Load all built-in language profiles (supports ~70 languages)
          List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
          
          // Build the language detector
          languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                  .withProfiles(languageProfiles)
                  .build();
          
          // Create a text object factory
          textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
          
          log.info("Language Detector initialized with {} profiles.", languageProfiles.size());
      } catch (IOException e) {
          log.error("Failed to initialize Language Detector", e);
          // We don't throw here to allow the service to start, but detection will fail gracefully
      }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() == 0) {
      throw new InvalidFileException("No file uploaded or file is empty.");
    }

    if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
      throw new InvalidFileException("Invalid file type. Only PDF files are allowed.");
    }
  }

  private int countWords(String text) {
      if (text == null || text.trim().isEmpty()) {
          return 0;
      }
      return text.trim().split("\\s+").length;
  }
  
  private String detectLanguage(String text) {
      if (languageDetector == null || text == null || text.trim().isEmpty()) {
          return "unknown";
      }
      try {
          TextObject textObject = textObjectFactory.forText(text);
          // The library returns com.google.common.base.Optional
          com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
          
          if (lang.isPresent()) {
              return lang.get().getLanguage();
          }
          return "unknown";
      } catch (Exception e) {
          log.warn("Language detection failed", e);
          return "unknown";
      }
  }

  public ExtractResponse extract(MultipartFile file) throws IOException {
    validateFile(file); 

    log.info("Starting PDF text extraction. Filename='{}', size={} bytes",
            file.getOriginalFilename(), file.getSize());

    long startTime = System.currentTimeMillis();

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected/encrypted and not supported.");
      }

      int totalPages = doc.getNumberOfPages();
      PDFTextStripper stripper = new PDFTextStripper();

      // 1. Extract full text
      String fullText = stripper.getText(doc).trim();
      int fullTextWordCount = countWords(fullText);
      
      // 2. Detect Language
      String language = detectLanguage(fullText);

      // 3. Extract per-page text
      List<PageText> pages = new ArrayList<>();
      for (int i = 1; i <= totalPages; i++) {
        stripper.setStartPage(i);
        stripper.setEndPage(i);
        String pageText = stripper.getText(doc).trim();
        int pageWordCount = countWords(pageText);

        pages.add(new PageText(i, pageText, pageWordCount));
      }

      long elapsed = System.currentTimeMillis() - startTime;
      log.info("PDF extraction completed in {} ms. Pages: {}. Words: {}. Lang: {}", elapsed, totalPages, fullTextWordCount, language);

      // Return immutable record with word count and language
      return new ExtractResponse(fullText, pages, totalPages, fullTextWordCount, language);

    } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
      throw new InvalidPasswordException("PDF is password-protected/encrypted and not supported.");
    } catch (IOException ex) {
      log.error("PDF extraction failed for '{}': File corruption or structural error.", file.getOriginalFilename(), ex);
      
      String msg = ex.getMessage().toLowerCase();

      if (msg.contains("end-of-file") || msg.contains("stream") || msg.contains("invalid") || msg.contains("corrupt")) {
        throw new InvalidFileException("The uploaded PDF document appears to be corrupt or malformed.");
      }
      throw ex;
    }
  }

  public Map<String, Object> extractMetadata(MultipartFile file) throws IOException {
    validateFile(file); 

    log.info("Starting metadata extraction for '{}'", file.getOriginalFilename());

    try (PDDocument doc = PDDocument.load(file.getBytes())) {

      if (doc.isEncrypted()) {
        throw new InvalidPasswordException("PDF is password-protected/encrypted. Metadata cannot be extracted.");
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
      throw new InvalidPasswordException("PDF is password-protected/encrypted. Metadata cannot be extracted.");
    } catch (IOException ex) {
      log.error("Metadata extraction failed for '{}': Structural error.", file.getOriginalFilename(), ex);
      throw ex;
    }
  }
}
