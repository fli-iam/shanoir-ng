/**
 * 
 */
package org.shanoir.ng.solr.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

/**
 * @author yyao
 *
 */
@Configuration
@EnableSolrRepositories(basePackages="org.shanoir.ng.solr.repository")
@ComponentScan
public class SolrConfig {
	
	@Value("${spring.data.solr.host}")
	private String solrHost;

	
	@Bean
	public HttpSolrClient solrClient() {
		return new HttpSolrClient(solrHost);
	}
	
    @Bean
    public SolrTemplate solrTemplate(SolrClient client) throws Exception {
        return new SolrTemplate(client);
    }

}
