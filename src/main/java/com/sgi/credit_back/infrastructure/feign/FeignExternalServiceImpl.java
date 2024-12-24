package com.sgi.credit_back.infrastructure.feign;

import com.sgi.credit_back.domain.ports.out.FeignExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FeignExternalServiceImpl implements FeignExternalService {

    private final WebClient webClient;

    public FeignExternalServiceImpl(WebClient.Builder webClientBuilder, @Value("${feign.client.config.transaction-service.url}") String transactionServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    public <T, R> Mono<R> post(String url, T requestBody, Class<R> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnNext(response -> log.info("Request to {} succeeded: {}", url, response))
                .onErrorResume(ex -> {
                    log.error("Error during request to {}", url, ex);
                    return Mono.error(new Exception("Error processing request", ex));
                });
    }

    public <R> Flux<R> get(String url, String productId, Class<R> responseType) {
        return webClient.get()
                .uri(url, productId)
                .retrieve()
                .bodyToFlux(responseType)
                .doOnNext(response -> log.info("Request to {} succeeded: {}", url, response))
                .onErrorResume(ex -> {
                    log.error("Error during request to {}", url, ex);
                    return Mono.error(new Exception("Error processing request", ex));
                });
    }

}