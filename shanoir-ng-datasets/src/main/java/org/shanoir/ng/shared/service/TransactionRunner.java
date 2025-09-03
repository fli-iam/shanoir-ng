package org.shanoir.ng.shared.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;

@Service
public class TransactionRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void runInTransaction(Consumer<EntityManager> action) {
        action.accept(entityManager);
    }
}
