package com.vishal.pdfapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for submitting a Base64-encoded file.")
public class JsonFilePayload {

    @Schema(description = "The Base64-encoded content of the file.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "JVBERi0xLjQKJ...")
    private String fileContent;

    // Getters and Setters
    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
