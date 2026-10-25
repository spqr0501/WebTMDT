package com.example.demo.dto;

import java.math.BigDecimal;

/** Chỉ trả về các thông tin công khai cần thiết cho ô gợi ý tìm kiếm. */
public record SearchSuggestion(Long id, String name, BigDecimal price,
                               String imageUrl, String description, boolean inStock) {}
