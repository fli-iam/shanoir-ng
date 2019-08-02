/**
 * 
 */
package org.shanoir.ng.solr.service;

import org.shanoir.ng.solr.model.ShanoirDocument;

/**
 * @author yyao
 *
 */
public interface SolrService {
	
	ShanoirDocument addToIndex (ShanoirDocument document);

}
