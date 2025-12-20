package com.vishal.pdfapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response containing extracted PDF text")
public record ExtractResponse(
        @Schema(description = "Full extracted text (concatenation of all pages)")
        String fullText,

        @Schema(description = "List of per-page extracted text")
        List<PageText> pages,

        @Schema(description = "Total number of pages extracted")
        int pageCount,

        @Schema(description = "Total word count of the full text")
        int wordCount,

        @Schema(description = "Detected language of the text (e.g., 'en', 'fr', 'es'). Returns 'unknown' if detection fails.")
        String language
) {}