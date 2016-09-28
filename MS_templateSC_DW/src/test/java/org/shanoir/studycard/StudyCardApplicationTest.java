package org.shanoir.studycard;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.shanoir.studycard.resources.StudyCardResource;

import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

/**
 * @author msimon
 *
 */
public class StudyCardApplicationTest {
	
	private final Environment environment = mock(Environment.class);
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
    private final StudyCardApplication application = new StudyCardApplication();
    private final StudyCardConfiguration config = new StudyCardConfiguration();

    @Before
    public void setup() throws Exception {
        when(environment.jersey()).thenReturn(jersey);
    }

    @Test
    public void buildsAThingResource() throws Exception {
        application.run(config, environment);

        verify(jersey).register(isA(StudyCardResource.class));
    }
}
