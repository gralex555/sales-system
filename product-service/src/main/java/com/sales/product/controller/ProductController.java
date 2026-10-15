package com.sales.product.controller;

import com.sales.product.dto.*;
import com.sales.product.entity.ReservationResult;
import com.sales.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get product by id")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all products", description = "Returns paginated list")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        Page<ProductResponse> responses = productService.getAllProducts(pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Reserve product stock")
    @ApiResponse(responseCode = "200", description = "Stock reserved")
    @ApiResponse(responseCode = "409", description = "Insufficient stock")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable Long id,
            @Valid @RequestBody ReserveStockRequest request) {

        ReservationResult result = productService.reserveStock(id, request.getQuantity());

        if (result == ReservationResult.SUCCESS) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @Operation(summary = "Release reserved stock")
    @ApiResponse(responseCode = "200", description = "Stock released")
    @ApiResponse(responseCode = "409", description = "Not enough reserved")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseStockRequest request) {

        productService.releaseStock(id, request.getQuantity());

        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Upload product certificate")
    @ApiResponse(responseCode = "204", description = "Сертификат загружен")
    @ApiResponse(responseCode = "400", description = "Файл пустой, не PDF или больше 10 МБ")
    @ApiResponse(responseCode = "404", description = "Товар не найден")
    @PostMapping(value = "/{id}/certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadCertificate(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        productService.attachCertificate(id, file);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить ссылку на сертификат")
    @ApiResponse(responseCode = "200", description = "Ссылка действует час")
    @ApiResponse(responseCode = "404", description = "Товар или сертификат не найден")
    @GetMapping("/{id}/certificate")
    public ResponseEntity<CertificateLinkResponse> getCertificate(@PathVariable Long id) {
        URL link = productService.getCertificateLink(id);
        return ResponseEntity.ok(new CertificateLinkResponse(link.toString()));
    }
}
