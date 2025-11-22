package com.vishal.pdfapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Response containing PDF metadata or an error message.")
public class PdfMetadataResponse {
    @Schema(
            description = "Whether the metadata extraction succeeded.",
            example = "true"
    )
    public boolean success;

    @Schema(
            description = "Metadata extracted from the PDF. Keys and values vary depending on the file.",
            example = "{\"Title\": \"Sample PDF\", \"Author\": \"John Doe\", \"Producer\": \"PDFBox\"}"
    )
    public Map<String, Object> metadata;

    @Schema(
            description = "Message describing failure if success = false.",
            example = "PDF is password-protected"
    )
    public String message;

    public PdfMetadataResponse(boolean success, Map<String, Object> metadata) {
        this.success = success;
        this.metadata = metadata;
    }

    public PdfMetadataResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
