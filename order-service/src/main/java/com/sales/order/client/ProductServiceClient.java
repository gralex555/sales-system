package com.sales.order.client;

import com.sales.order.client.dto.ProductInfo;
import com.sales.order.client.dto.ReserveStockRequest;
import com.sales.order.exception.InsufficientStockException;
import com.sales.order.exception.ProductNotAvailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {
    private final RestClient restClient;

    public ProductServiceClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    public ProductInfo getProduct(Long productId) {
        try {
            return restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductInfo.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotAvailableException(productId);
        }
    }


    public void reserveStock(Long productId, Integer quantity) {
        try {
            restClient.post()
                    .uri("/api/v1/products/{id}/reserve", productId)
                    .body(new ReserveStockRequest(quantity))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotAvailableException(productId);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new InsufficientStockException(productId, quantity);
        }
    }
}
