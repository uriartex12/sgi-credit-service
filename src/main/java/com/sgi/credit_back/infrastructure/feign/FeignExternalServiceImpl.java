package com.sgi.credit_back.infrastructure.feign;

import com.sgi.credit_back.domain.ports.out.FeignExternalService;
import com.sgi.credit_back.domain.shared.CustomError;
import com.sgi.credit_back.infrastructure.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static com.sgi.credit_back.domain.shared.Constants.*;

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
                .doOnNext(response -> logSuccess(url, response))
                .onErrorResume(ex -> {
                    log.error(EXTERNAL_REQUEST_ERROR_FORMAT, url, ex);
                    return Mono.error(new CustomException(CustomError.E_OPERATION_FAILED));
                });
    }

    public <R> Flux<R> get(String url, String pathVariable, Class<R> responseType) {
        return webClient.get()
                .uri(url, pathVariable)
                .retrieve()
                .bodyToFlux(responseType)
                .doOnNext(response -> logSuccess(url, response))
                .onErrorResume(ex -> {
                    log.error(EXTERNAL_REQUEST_ERROR_FORMAT, url, ex);
                    return Mono.error(new CustomException(CustomError.E_OPERATION_FAILED));
                });
    }

    private <R> void logSuccess(String url, R response) {
        log.info(EXTERNAL_REQUEST_SUCCESS_FORMAT, url, response);
    }

}