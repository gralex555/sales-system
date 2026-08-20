package com.sales.order.config;

import com.sales.order.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(
            @Value("${services.product.url}") String productServiceUrl,
            @Value("${services.product.connect-timeout:2s}") Duration connectTimeout,
            @Value("${services.product.read-timeout:5s}") Duration readTimeout) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String correlationId = MDC.get("correlationId");
                    if (correlationId != null) {
                        request.getHeaders().add(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }  // requestInterceptor - перехватчик исходящих запросов. Перед отправкой достаёт ID из MDC и кладёт в заголовок.
}
