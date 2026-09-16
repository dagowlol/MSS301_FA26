package com.fudn.product_service.service;

import com.fudn.product_service.dto.ProductRequest;
import com.fudn.product_service.dto.ProductResponse;
import com.fudn.product_service.model.Product;
import com.fudn.product_service.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final IProductRepository productRepository;

    /**
     * Bước 6.2: Triển khai phương thức tạo sản phẩm
     */
    public ProductResponse createProduct(ProductRequest productRequest) {
        // 1. Sử dụng Builder Pattern để tạo Product từ ProductRequest (Java Record)
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .build();

        // 2. Lưu vào database
        Product savedProduct = productRepository.save(product);

        // 3. Ghi log thông báo sản phẩm đã được lưu
        log.info("Product {} is saved successfully", savedProduct.getId());

        // 4. Trả về đối tượng ProductResponse chứa đầy đủ thông tin (bao gồm ID)
        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice()
        );
    }

    /**
     * Bước 6.3: Triển khai phương thức lấy danh sách sản phẩm
     */
    public List<ProductResponse> getAllProducts() {
        // 1. Lấy toàn bộ sản phẩm từ DB
        List<Product> products = productRepository.findAll();

        // 2. Sử dụng Java Stream API để ánh xạ từ Product sang ProductResponse
        return products.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice()
                ))
                .toList(); // Thu thập kết quả trả về bằng .toList() (Java 16+)
    }
}