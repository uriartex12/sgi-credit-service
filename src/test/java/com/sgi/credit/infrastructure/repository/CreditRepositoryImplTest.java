package com.sgi.credit.infrastructure.repository;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.helper.FactoryTest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.mapper.CreditMapper;
import com.sgi.credit.infrastructure.repository.impl.CreditRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the CreditRepositoryImpl class.
 * This class contains test cases that verify the functionality of the
 * CreditRepositoryImpl implementation. It uses mocks to test the
 * repository methods in isolation.
 */
@ExtendWith(MockitoExtension.class)
public class CreditRepositoryImplTest {

    @InjectMocks
    private CreditRepositoryImpl creditRepository;

    @Mock
    private CreditRepositoryJpa repositoryJpa;

    @Test
    public void testSave() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        CreditResponse accountResponse = CreditMapper.INSTANCE.toCreditResponse(credit);
        when(repositoryJpa.save(credit)).thenReturn(Mono.just(credit));
        Mono<CreditResponse> result = creditRepository.save(credit);
        StepVerifier.create(result)
                .expectNext(accountResponse)
                .verifyComplete();

        verify(repositoryJpa, times(1)).save(credit);
    }


    @Test
    public void testFindById() {
        String creditId = UUID.randomUUID().toString();
        Credit credit =  FactoryTest.toFactoryEntityCredit();
        when(repositoryJpa.findById(creditId))
                .thenReturn(Mono.just(credit));
        Mono<Credit> result = creditRepository.findById(creditId);
        StepVerifier.create(result)
                .expectNext(credit)
                .verifyComplete();

        verify(repositoryJpa, times(1)).findById(creditId);
    }

    @Test
    public void testFindAll() {
        Credit credit1 = FactoryTest.toFactoryEntityCredit();
        Credit credit2 = FactoryTest.toFactoryEntityCredit();
        when(repositoryJpa.findAll()).thenReturn(Flux.just(credit1, credit2));
        Flux<CreditResponse> result = creditRepository.findAll();
        result.collectList().subscribe(responses -> {
            assertNotNull(responses);
            assertEquals(2, responses.size());
        });

        verify(repositoryJpa, times(1)).findAll();
    }

    @Test
    public void testDelete() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setId(UUID.randomUUID().toString());
        when(repositoryJpa.delete(credit)).thenReturn(Mono.empty());
        Mono<Void> result = creditRepository.delete(credit);
        StepVerifier.create(result)
                .verifyComplete();
        verify(repositoryJpa, times(1)).delete(credit);
    }

    @Test
    public void getCreditCardByClientId() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setId(UUID.randomUUID().toString());
        when(repositoryJpa.findAllByClientId(credit.getClientId())).thenReturn(Flux.empty());
        Flux<CreditResponse> result = creditRepository.getCreditCardByClientId(credit.getClientId());
        StepVerifier.create(result)
                .verifyComplete();
        verify(repositoryJpa, times(1)).findAllByClientId(credit.getClientId());
    }
}
