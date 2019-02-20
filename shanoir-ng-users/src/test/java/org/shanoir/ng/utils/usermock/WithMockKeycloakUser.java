package org.shanoir.ng.utils.usermock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = MockKeycloakUserContextFactory.class)
public @interface WithMockKeycloakUser {
	
	long id();
	
	String value() default "user";

    String username() default "";

    String[] roles() default {"USER"};
    
    String[] authorities() default {};

    String password() default "password";
    
}

