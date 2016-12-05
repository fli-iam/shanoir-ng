package org.shanoir.ng.service.impl;

import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User details service implementation.
 * 
 * @author msimon
 *
 */
@Service("userDetailsService")
public class ShanoirUserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final User user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("Username " + username + " not found");
		}
		return user;
	}

}
