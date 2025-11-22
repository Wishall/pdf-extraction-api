package com.vishal.pdfapi.model;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing extracted PDF text")
public class ExtractResponse {
  @Schema(description = "Whether extraction succeeded")
  public boolean success;

  @Schema(description = "Full extracted text")
  public String text;

  @Schema(description = "List of per-page extracted text")
  public List<PageText> pages;
  public ExtractResponse(){}
  public ExtractResponse(boolean s,String t){success=s;text=t;}

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public List<PageText> getPages() {
    return pages;
  }

  public void setPages(List<PageText> pages) {
    this.pages = pages;
  }
}