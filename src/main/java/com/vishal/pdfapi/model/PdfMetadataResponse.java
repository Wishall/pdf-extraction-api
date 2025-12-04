package com.vishal.pdfapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Response containing PDF metadata.")
public record PdfMetadataResponse(
        @Schema(
                description = "Metadata extracted from the PDF. Keys and values vary depending on the file."
        )
        Map<String, Object> metadata
) {}