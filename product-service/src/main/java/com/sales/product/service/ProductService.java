package com.sales.product.service;

import com.sales.product.dto.CreateProductRequest;
import com.sales.product.dto.ProductResponse;
import com.sales.product.entity.Product;

import com.sales.product.entity.ReservationResult;
import com.sales.product.exception.ProductNotFoundException;
import com.sales.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantityAvailable(request.getQuantityAvailable());
        product.setQuantityReserved(0);

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setQuantityAvailable(product.getQuantityAvailable());
        response.setQuantityReserved(product.getQuantityReserved());
        return response;
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ReservationResult reserveStock(Long productId, Integer quantity) {
        // проверим, что товар вообще существует
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        int updated = productRepository.reserveStock(productId, quantity);

        return updated > 0
                ? ReservationResult.SUCCESS
                : ReservationResult.INSUFFICIENT_STOCK;
    }

}
