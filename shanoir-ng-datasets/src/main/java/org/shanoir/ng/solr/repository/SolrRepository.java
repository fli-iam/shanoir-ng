/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.List;

import org.shanoir.ng.solr.model.ShanoirDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

/**
 * @author yyao
 *
 */
public interface SolrRepository extends SolrCrudRepository<ShanoirDocument, String> {

	/**
	 * @param datasetName
	 * @return
	 */
	public List<ShanoirDocument> findByDatasetName(String datasetName);
	
//	@Query("id:*?0* OR name:*?0*")
//    public Page<ShanoirDocument> findByCustomQuery(String searchTerm, Pageable pageable);
//
//    @Query(name = "Product.findByNamedQuery")
//    public Page<ShanoirDocument> findByNamedQuery(String searchTerm, Pageable pageable);

}
