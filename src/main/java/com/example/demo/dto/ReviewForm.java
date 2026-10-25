package com.example.demo.dto;
import jakarta.validation.constraints.*;
public class ReviewForm {
@NotNull @Min(1) @Max(5) private Integer rating;
public Integer getRating() { return rating; }
public void setRating(Integer v) { rating=v; }
@NotBlank @Size(min=10,max=2000) private String comment;
public String getComment() { return comment; }
public void setComment(String v) { comment=v; }
}
