/**
 * 
 */
package org.shanoir.ng.configuration.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author msimon
 *
 */
public class UserDetailsArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private RoleService roleService;

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		if (supportsParameter(methodParameter)) {
			return createUserDetails(webRequest);
		} else {
			return WebArgumentResolver.UNRESOLVED;
		}
	}

	@SuppressWarnings("unchecked")
	private Object createUserDetails(NativeWebRequest webRequest) {
		KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) webRequest
				.getUserPrincipal();

		AccessToken token = principal.getKeycloakSecurityContext().getToken();

		final UserContext userContext = new UserContext();
		userContext.setId(Long.valueOf(token.getId()));
		userContext.setUsername(token.getPreferredUsername());
		final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if (token.getRealmAccess().getRoles() != null) {
			for (String roleStr : token.getRealmAccess().getRoles()) {
				Optional<Role> role = roleService.findByName(roleStr);
				if (role.isPresent()) {
					authorities.add(role.get());
				}
			}
		}
		userContext.setAuthorities(authorities);
		return userContext;
	}

	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		// return methodParameter.getParameterAnnotation(CurrentUser.class) !=
		// null
		// && methodParameter.getParameterType().equals(UserContext.class);
		return methodParameter.getParameterType().equals(UserContext.class);
	}

}
