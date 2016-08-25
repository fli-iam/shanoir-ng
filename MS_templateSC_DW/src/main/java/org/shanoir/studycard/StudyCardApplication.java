package org.shanoir.studycard;

import org.shanoir.studycard.module.StudyCardModule;

import com.hubspot.dropwizard.guice.GuiceBundle;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Project application class.
 * 
 * @author msimon
 *
 */
public class StudyCardApplication extends Application<StudyCardConfiguration> {

	public static void main(String[] args) throws Exception {
		new StudyCardApplication().run(args);
	}
	
	@Override
	public void initialize(Bootstrap<StudyCardConfiguration> bootstrap) {
		// Load guice module
		GuiceBundle<StudyCardConfiguration> guiceBundle = GuiceBundle.<StudyCardConfiguration>newBuilder()
                .addModule(new StudyCardModule(bootstrap))
                .setConfigClass(StudyCardConfiguration.class)
                .enableAutoConfig(getClass().getPackage().getName())
                .build();
        bootstrap.addBundle(guiceBundle);
	}
	
	@Override
	public void run(StudyCardConfiguration configuration, Environment environment) throws Exception {
		// TODO Auto-generated method stub
	}
	
}
