package org.shanoir.ng.service.impl;

import java.util.Arrays;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException {
		// MOCK !!!
		// Replace by DAO access
		return new User("user", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
	}

}
