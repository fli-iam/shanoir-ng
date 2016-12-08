package org.shanoir.ng.repository;

import java.util.List;

import org.shanoir.ng.model.User;

public interface UserRepositorySpecific {

	List<User> findBy(String fieldName, Object value);

}
