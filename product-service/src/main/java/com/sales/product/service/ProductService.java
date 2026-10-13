package com.sales.product.service;

import com.sales.product.dto.CreateProductRequest;
import com.sales.product.dto.ProductResponse;
import com.sales.product.entity.Product;

import com.sales.product.entity.ReservationResult;
import com.sales.product.exception.CertificateNotFoundException;
import com.sales.product.exception.InsufficientReservationException;
import com.sales.product.exception.ProductNotFoundException;
import com.sales.product.repository.ProductRepository;
import com.sales.product.storage.CertificateStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CertificateStorage certificateStorage;
    private final ProductCertificateUpdater certificateUpdater;

    public ProductService(ProductRepository productRepository, CertificateStorage certificateStorage, ProductCertificateUpdater certificateUpdater) {
        this.productRepository = productRepository;
        this.certificateStorage = certificateStorage;
        this.certificateUpdater = certificateUpdater;
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
        log.info("Reserving {} units of product {}", quantity, productId);
        // проверим, что товар вообще существует
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        int updated = productRepository.reserveStock(productId, quantity);

        ReservationResult result = updated > 0
                ? ReservationResult.SUCCESS
                : ReservationResult.INSUFFICIENT_STOCK;

        log.info("Reservation result for product {}: {}", productId, result);

        return result;
    }

    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        log.info("Releasing {} units of product {}", quantity, productId);

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        int updated = productRepository.releaseStock(productId, quantity);

        if (updated == 0) {
            throw new InsufficientReservationException(productId, quantity);
        }

        log.info("Released {} units of product {}", quantity, productId);
    }

    public void attachCertificate(Long productId, MultipartFile file) {
        validate(file);

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        String key = certificateStorage.upload(productId, file);

        certificateUpdater.saveCertificate(productId, key, file.getOriginalFilename());
    }

    @Transactional(readOnly = true)
    public URL getCertificateLink(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getCertificateKey() == null) {
            throw new CertificateNotFoundException(productId);
        }

        return certificateStorage.generateDownloadLink(
                product.getCertificateKey(),
                product.getCertificateFilename());
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Сертификат принимается только в PDF");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Файл больше 10 МБ");
        }
    }


}
