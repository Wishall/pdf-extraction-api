package com.vishal.pdfapi.model;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Extracted text of a single PDF page")
public class PageText {

  @Schema(description = "Page number")
  public int page;

  @Schema(description = "Extracted text for this page")
  public String text;

  public PageText(int p,String t){page=p;text=t;}

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}