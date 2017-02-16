package org.shanoir.ng.configuration.security;

import java.util.Arrays;
import java.util.List;

import org.shanoir.ng.configuration.security.jwt.JwtAuthenticationProvider;
import org.shanoir.ng.configuration.security.jwt.JwtAuthenticationProcessingFilter;
import org.shanoir.ng.configuration.security.jwt.ShanoirAuthenticationProvider;
import org.shanoir.ng.configuration.security.jwt.SkipPathRequestMatcher;
import org.shanoir.ng.configuration.security.jwt.extractor.TokenExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring security configuration.
 *
 * @author msimon
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	public static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";
	public static final String FORM_BASED_LOGIN_ENTRY_POINT = "/authenticate";
	public static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/**";
	public static final String TOKEN_REFRESH_ENTRY_POINT = "/authenticate/token";
	public static final String USER_ACCOUNT_REQUEST_ENTRY_POINT = "/user/accountrequest";

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private JwtAuthenticationProvider jwtAuthenticationProvider;

	@Autowired
	private ShanoirAuthenticationProvider shanoirAuthenticationProvider;

	@Autowired
	private TokenExtractor tokenExtractor;

	@Autowired
	private ShanoirLogoutSuccess logoutSuccess;

	protected JwtAuthenticationProcessingFilter buildJwtAuthenticationProcessingFilter() throws Exception {
		List<String> pathsToSkip = Arrays.asList(TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT, USER_ACCOUNT_REQUEST_ENTRY_POINT);
		SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip, TOKEN_BASED_AUTH_ENTRY_POINT);
		JwtAuthenticationProcessingFilter filter = new JwtAuthenticationProcessingFilter(
				authenticationFailureHandler, tokenExtractor, matcher);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
				// We don't need CSRF for JWT based authentication
				.csrf().disable().exceptionHandling()

				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

				.and().authorizeRequests()
				// Login end-point
				.antMatchers(FORM_BASED_LOGIN_ENTRY_POINT).permitAll()
				// Token refresh end-point
				.antMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll()
				// User account request end-point
				.antMatchers(USER_ACCOUNT_REQUEST_ENTRY_POINT).permitAll()

				.and().authorizeRequests()
				// Protected API End-points
				.antMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated()

				.and().formLogin().loginPage("http://localhost/login")

				.and().logout().logoutSuccessHandler(logoutSuccess)

				.and().addFilterBefore(buildJwtAuthenticationProcessingFilter(),
						UsernamePasswordAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(shanoirAuthenticationProvider);
		auth.authenticationProvider(jwtAuthenticationProvider);
	}

	/**
	 * http://stackoverflow.com/a/31748398/122441 until
	 * https://jira.spring.io/browse/DATAREST-573
	 * 
	 * @return
	 */
	@Bean
	public FilterRegistrationBean corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		source.registerCorsConfiguration("/**", config);
		final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	@Bean
	public HttpSessionStrategy httpSessionStrategy() {
		return new HeaderHttpSessionStrategy();
	}

}
