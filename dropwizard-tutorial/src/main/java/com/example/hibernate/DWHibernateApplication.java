package com.example.hibernate;

import com.example.hibernate.core.UserDAO;
import com.example.hibernate.jdbi.User;
import com.example.hibernate.resources.UserResource;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author msimon
 *
 */
public class DWHibernateApplication extends Application<DWHibernateConfiguration> {

	private final HibernateBundle<DWHibernateConfiguration> hibernate = new HibernateBundle<DWHibernateConfiguration>(User.class) {
		public DataSourceFactory getDataSourceFactory(DWHibernateConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};
	
	public static void main(String[] args) throws Exception {
		new DWHibernateApplication().run(args);
	}
	
	@Override
	public String getName() {
		return "hibernate";
	}
	
	@Override
	public void initialize(Bootstrap<DWHibernateConfiguration> bootstrap) {
		bootstrap.addBundle(hibernate);
	}
	
	@Override
	public void run(DWHibernateConfiguration configuration, Environment environment)
			throws Exception {
		final UserDAO dao = new UserDAO(hibernate.getSessionFactory());
	    environment.jersey().register(new UserResource(dao));
	}

}
