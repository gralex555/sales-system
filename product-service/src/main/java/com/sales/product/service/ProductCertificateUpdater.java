package com.sales.product.service;

import com.sales.product.entity.Product;
import com.sales.product.exception.ProductNotFoundException;
import com.sales.product.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductCertificateUpdater {
    private final ProductRepository productRepository;

    public ProductCertificateUpdater(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void saveCertificate(Long productId, String key, String filename) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setCertificateKey(key);
        product.setCertificateFilename(filename);

    }
}
