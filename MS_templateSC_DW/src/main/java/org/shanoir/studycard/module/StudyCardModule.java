package org.shanoir.studycard.module;

import org.hibernate.SessionFactory;
import org.shanoir.studycard.StudyCardConfiguration;
import org.shanoir.studycard.core.StudyCardDAO;
import org.shanoir.studycard.core.impl.StudyCardDAOImpl;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;
import org.shanoir.studycard.service.impl.StudyCardServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;

/**
 * Guice module for application.
 * 
 * @author msimon
 *
 */
public class StudyCardModule extends AbstractModule {

	private final HibernateBundle<StudyCardConfiguration> hibernateBundle = new HibernateBundle<StudyCardConfiguration>(
			StudyCard.class) {
		public DataSourceFactory getDataSourceFactory(StudyCardConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	/**
	 * Constructor.
	 * 
	 * @param bootstrap bootstrap.
	 */
	public StudyCardModule(Bootstrap<StudyCardConfiguration> bootstrap) {
		bootstrap.addBundle(hibernateBundle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		binder().bind(StudyCardDAO.class).to(StudyCardDAOImpl.class);
		binder().bind(StudyCardService.class).to(StudyCardServiceImpl.class);
	}

	@Provides
	public SessionFactory provideSessionFactory() {
		return hibernateBundle.getSessionFactory();
	}

}
