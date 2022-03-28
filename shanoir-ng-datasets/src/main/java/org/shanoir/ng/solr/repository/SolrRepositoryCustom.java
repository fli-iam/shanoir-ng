/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.Collection;
import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.paging.FacetPageable;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.SolrResultPage;

/**
 * @author yyao
 *
 */
public interface SolrRepositoryCustom {
	
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteria(ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException;
	
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Collection<Long> studyIds, ShanoirSolrQuery query, Pageable pageable) throws RestServiceException;
}
