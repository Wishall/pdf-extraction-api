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
        int pageCount
) {}