package org.shanoir.ng;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.shanoir.ng.solr.service.SolrServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
@Profile("!test")
public class ShanoirDatasetIndexation implements ApplicationRunner {

    @Autowired
    private SolrServiceImpl solrServiceImpl;

    @Value("${spring.data.solr.host}")
    private String solrUrl;

    private static final int RETRY_INTERVAL_MS = 5000;

    private static final int MAX_WAIT_TIME_MS = 10 * 60 * 1000;

    private static final Logger LOG = LoggerFactory.getLogger(ShanoirDatasetIndexation.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SolrClient solrClient = null;
        long startTime = System.currentTimeMillis();

        while (Objects.isNull(solrClient) && (System.currentTimeMillis() - startTime) < MAX_WAIT_TIME_MS) {
            try {
                solrClient = new HttpSolrClient.Builder(solrUrl).build();
            } catch (Exception ignored) {
                Thread.sleep(RETRY_INTERVAL_MS);
            }
        }

        if(Objects.nonNull(solrClient)){
            SolrQuery q = new SolrQuery("*:*");
            q.setRows(0);  // don't actually request any data
            if (Objects.nonNull(solrClient.query(q)) && Objects.equals(0L, solrClient.query(q).getResults().getNumFound())) {
                LOG.info("Solr index empty. Re-indexing...");
                solrServiceImpl.indexAllNoAuth();
                LOG.info("Solr indexation complete.");
            } else {
                LOG.info("Solr index already complete, no re-indexation required.");
            }
        }
    }
}

