package org.shanoir.ng.key.repository;

import org.shanoir.ng.key.model.KeyValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyValueRepository extends JpaRepository<KeyValue, String> {
}
