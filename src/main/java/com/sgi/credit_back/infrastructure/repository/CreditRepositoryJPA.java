package com.sgi.credit_back.infrastructure.repository;

import com.sgi.credit_back.domain.model.Credit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CreditRepositoryJPA extends ReactiveMongoRepository<Credit,String> {}
