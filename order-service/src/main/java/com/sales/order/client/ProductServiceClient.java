package com.sales.order.client;

import com.sales.order.client.dto.ProductInfo;
import com.sales.order.client.dto.ReserveStockRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {
    private final RestClient restClient;

    public ProductServiceClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    public ProductInfo getProduct(Long productId) {
        return restClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .body(ProductInfo.class);
    }

    public void reserveStock(Long productId, Integer quantity) {
        restClient.post()
                .uri("/api/v1/products/{id}/reserve", productId)
                .body(new ReserveStockRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
}
