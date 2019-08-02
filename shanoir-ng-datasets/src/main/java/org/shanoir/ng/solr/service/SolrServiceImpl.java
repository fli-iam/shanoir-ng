/**
 * 
 */
package org.shanoir.ng.solr.service;

import java.util.List;

import org.shanoir.ng.solr.model.ShanoirDocument;
import org.shanoir.ng.solr.repository.SolrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yyao
 *
 */
@Service
public class SolrServiceImpl implements SolrService{
	
	@Autowired 
	SolrRepository repository;
	
	@Override
	public ShanoirDocument addToIndex (final ShanoirDocument document) {
		
		return repository.save(document);
	} 
	
	public Iterable<ShanoirDocument> getDocuments() {
		return repository.findAll();
	}
	
	public List<ShanoirDocument> findDocumentByDatasetName(String datasetName) {
		return repository.findByDatasetName(datasetName);
	}

}
