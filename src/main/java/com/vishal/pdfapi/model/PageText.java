package com.vishal.pdfapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Extracted text of a single PDF page")
public record PageText(
        @Schema(description = "Page number (1-based index)")
        int pageNumber,

        @Schema(description = "Extracted text for this page")
        String text,

        @Schema(description = "Word count for this page")
        int wordCount
) {}