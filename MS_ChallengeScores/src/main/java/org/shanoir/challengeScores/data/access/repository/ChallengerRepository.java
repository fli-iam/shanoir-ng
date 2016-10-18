package org.shanoir.challengeScores.data.access.repository;

import org.shanoir.challengeScores.data.model.Challenger;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring repository interface.
 *
 * Allows to use basic CRUD methods on the parameterized type without even implementing this interface.
 * See {@link CrudRepository}.
 *
 * @author jlouis
 */
public interface ChallengerRepository extends CrudRepository<Challenger, Long> {


}
