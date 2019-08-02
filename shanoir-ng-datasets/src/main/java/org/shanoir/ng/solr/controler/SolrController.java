/**
 * 
 */
package org.shanoir.ng.solr.controler;

import org.shanoir.ng.solr.model.ShanoirDocument;
import org.shanoir.ng.solr.service.SolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yyao
 *
 */
@Controller
public class SolrController {
	
	@Autowired
	private SolrService solrService;
	
	@Transactional 
	public ShanoirDocument addToIndex() {
		ShanoirDocument document = new ShanoirDocument();
		document.setDatasetId("1");
		document.setDatasetName("test");
		
		return solrService.addToIndex(document);
	} 
}