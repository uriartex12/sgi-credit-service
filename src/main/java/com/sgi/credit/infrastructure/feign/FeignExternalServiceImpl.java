package com.sgi.credit.infrastructure.feign;

import com.sgi.credit.domain.ports.out.FeignExternalService;
import com.sgi.credit.domain.shared.CustomError;
import com.sgi.credit.infrastructure.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.sgi.credit.domain.shared.Constants.EXTERNAL_REQUEST_ERROR_FORMAT;
import static com.sgi.credit.domain.shared.Constants.EXTERNAL_REQUEST_SUCCESS_FORMAT;

/**
 * Implementation of the external Feign service to make HTTP requests.
 * Uses WebClient to make reactive requests to an external service.
 */
@Service
@Slf4j
public class FeignExternalServiceImpl implements FeignExternalService {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public FeignExternalServiceImpl(WebClient.Builder webClientBuilder,
                                    @Value("${feign.client.config.transaction-service.url}") String transactionServiceUrl,
                                    ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.circuitBreaker =  circuitBreakerFactory.create("credit-service");
        this.webClient = webClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    @Override
    public <T, R> Mono<R> post(String url, T requestBody, Class<R> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnNext(response -> logSuccess(url, response))
                .onErrorResume(ex -> Mono.error(new CustomException(CustomError.E_OPERATION_FAILED)))
                .transformDeferred(circuitBreaker::run);
    }

    @Override
    public <R> Flux<R> get(String url, String pathVariable, Class<R> responseType) {
        return webClient.get()
                .uri(url, pathVariable)
                .retrieve()
                .bodyToFlux(responseType)
                    .doOnNext(response -> logSuccess(url, response))
                .doOnError(ex -> logError(url, ex))
                .onErrorResume(ex -> Mono.error(new CustomException(CustomError.E_OPERATION_FAILED)))
                .transformDeferred(circuitBreaker::run);
    }

    private <R> void logSuccess(String url, R response) {
        log.info(EXTERNAL_REQUEST_SUCCESS_FORMAT, url, response);
    }

    private void logError(String url, Throwable ex) {
        log.error(EXTERNAL_REQUEST_ERROR_FORMAT, url, ex);
    }
}
