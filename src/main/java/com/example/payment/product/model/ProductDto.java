package com.example.payment.product.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDto {
    private String name;
    private Integer price;
}
