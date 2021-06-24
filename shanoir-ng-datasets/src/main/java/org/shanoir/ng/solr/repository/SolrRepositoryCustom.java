/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.Collection;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrFacet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.SolrResultPage;

/**
 * @author yyao
 *
 */
public interface SolrRepositoryCustom {
	
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteria(ShanoirSolrFacet facet,Pageable pageable) throws RestServiceException;
	
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Collection<Long> studyIds, ShanoirSolrFacet facet,Pageable pageable) throws RestServiceException;
}
