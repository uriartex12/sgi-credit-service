package com.sgi.credit_back.domain.ports.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FeignExternalService {
    <T, R> Mono<R> post(String url, T requestBody, Class<R> responseType);
    <R> Flux<R> get(String url,  String productId, Class<R> responseType);
}
