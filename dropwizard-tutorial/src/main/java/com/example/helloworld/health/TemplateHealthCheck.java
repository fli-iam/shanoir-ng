package com.example.helloworld.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * @author msimon
 *
 */
public class TemplateHealthCheck extends HealthCheck {

	private final String template;
	
	public TemplateHealthCheck(final String template) {
		this.template = template;
	}
	
	/* (non-Javadoc)
	 * @see com.codahale.metrics.health.HealthCheck#check()
	 */
	@Override
	protected Result check() throws Exception {
		final String saying = String.format(template, "TEST");
		if (!saying.contains("TEST")) {
			return Result.unhealthy("Template doesn't include a name");
		}
		return Result.healthy();
	}

}
