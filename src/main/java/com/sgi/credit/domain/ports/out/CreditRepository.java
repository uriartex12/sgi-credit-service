package com.sgi.credit.domain.ports.out;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface defining operations to manage credits.
 */
public interface CreditRepository {

    /**
     * Saves a credit in the repository.
     *
     * @param credit the credit information to save
     * @return a Mono containing the saved credit response
     */
    Mono<CreditResponse> save(Credit credit);

    /**
     * Finds a credit by its ID.
     *
     * @param id the unique identifier of the credit
     * @return a Mono containing the found credit or empty if not found
     */
    Mono<Credit> findById(String id);

    /**
     * Retrieves all available credits.
     *
     * @return a Flux containing all credit responses
     */
    Flux<CreditResponse> findAll();

    /**
     * Deletes a credit from the repository.
     *
     * @param credit the credit information to delete
     * @return a Mono representing the deletion operation
     */
    Mono<Void> delete(Credit credit);
}
