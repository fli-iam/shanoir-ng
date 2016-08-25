package org.shanoir.studycard.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

/**
 * Resource class for study cards.
 * 
 * @author msimon
 *
 */
@Path("/studycard")
@Produces(MediaType.APPLICATION_JSON)
public class StudyCardResource {

	private StudyCardService studyCardService;

	/**
	 * @param studyCardService the studyCardService to set
	 */
	@Inject
	public void setStudyCardService(StudyCardService studyCardService) {
		this.studyCardService = studyCardService;
	}
	
    @GET
    @UnitOfWork
    public List<StudyCard> findAll() {
        return studyCardService.findAll();
    }
    
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Optional<StudyCard> findById(@PathParam("id") LongParam id) {
        return studyCardService.findById(id.get());
    }
    
    @POST
    @UnitOfWork
    public StudyCard createStudyCard(StudyCard newStudyCard) {
        return studyCardService.save(newStudyCard);
    }
    
    @PUT
    @Path("/{id}")
    @UnitOfWork
    public StudyCard updateStudyCard(StudyCard studyCard, @PathParam("id") LongParam id) {
    	Preconditions.checkArgument(id.get().equals(studyCard.getId()));
        return studyCardService.update(studyCard);
    }
    
}
