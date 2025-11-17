package org.shanoir.ng.key.service;

import org.shanoir.ng.key.model.KeyValue;
import org.shanoir.ng.key.repository.KeyValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class KeyValueService {

    @Autowired
    private KeyValueRepository repository;

    @Transactional
    public String getValue(String key) {
        return repository.findById(key)
                .map(KeyValue::getValue)
                .orElse(null);
    }

}