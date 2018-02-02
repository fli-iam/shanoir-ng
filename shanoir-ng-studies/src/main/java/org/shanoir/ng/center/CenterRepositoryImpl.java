package org.shanoir.ng.center;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for centers.
 * 
 * @author msimon
 *
 */
@Component
public class CenterRepositoryImpl implements CenterRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Center> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT c FROM Center c WHERE c.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM center", "centerNameResult").getResultList();
	}
	
	
	//@Override
	public List<IdNameDTO> findIdsAndNamesForCenter(Long centerId) {
	
		/*final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT c.acquisitionEquipments FROM Center c WHERE c.id LIKE :centerId");
		
		
		
		Query q = em.createNativeQuery("SELECT a.firstname, a.lastname FROM Author a");
		List<Object[]> authors = q.getResultList();
		 
		for (Object[] a : authors) {
		    System.out.println("Author "
		            + a[0]
		            + " "
		            + a[1]);
		}*/
		return null;
	}
	
}
