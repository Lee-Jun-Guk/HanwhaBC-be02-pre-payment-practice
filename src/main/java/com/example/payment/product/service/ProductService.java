package com.example.payment.product.service;

import com.example.payment.product.model.Product;
import com.example.payment.product.model.ProductDto;
import com.example.payment.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void create(ProductDto productDto) {
        productRepository.save(Product.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .build());
    }

    public List<ProductDto> list() {
        List<Product> result = productRepository.findAll();

        List<ProductDto> productDtos = new ArrayList<>();
        for (Product product : result) {

            ProductDto productDto = ProductDto.builder()
                    .name(product.getName())
                    .price(product.getPrice())
                    .build();
            productDtos.add(productDto);
        }
        return productDtos;
    }

    public ProductDto read(Integer id) {
        Optional<Product> result = productRepository.findById(id);
        if (result.isPresent()) {
            Product product = result.get();
            return ProductDto.builder()
                    .name(product.getName())
                    .price(product.getPrice())

                    .build();
        } else {
            return null;
        }

    }


    public void update(ProductDto productDto) {
        productRepository.save(Product.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .build());
    }



    public void delete(Integer id) {
        productRepository.delete(Product.builder().id(id).build());


//    }
    }}