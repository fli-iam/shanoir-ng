/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.solr.runner;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
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


@Component
@Profile("!test")
public class ShanoirDatasetIndexation implements ApplicationRunner {

    @Autowired
    private SolrServiceImpl solrServiceImpl;

    @Value("${solr.host}")
    private String solrUrl;

    private static final int RETRY_INTERVAL_MS = 1000;

    private static final int MAX_WAIT_TIME_MS = 10 * 60 * 1000;

    private static final Logger LOG = LoggerFactory.getLogger(ShanoirDatasetIndexation.class);

    @Override
    public void run(ApplicationArguments args) {
        SolrClient solrClient = new HttpSolrClient.Builder(solrUrl).build();
        SolrQuery q = new SolrQuery("*:*");
        q.setRows(0);  // don't actually request any data
        long deadline = System.currentTimeMillis() + MAX_WAIT_TIME_MS;
        try {
            while (true) {
                try {
                    if (Objects.nonNull(solrClient.query(q))
                            && Objects.equals(0L, solrClient.query(q).getResults().getNumFound())) {
                        LOG.info("Solr index empty. Re-indexing...");
                        solrServiceImpl.indexAllNoAuth();
                        LOG.info("Solr indexation complete.");
                    } else {
                        LOG.info("Solr index already complete, no re-indexation required.");
                    }
                    return;
                }
                catch (ConnectException e) {
                    if (System.currentTimeMillis() > deadline) {
                        LOG.warn("Solr index not checked (server not reachable)");
                        return;
                    }
                }
                Thread.sleep(RETRY_INTERVAL_MS);
            }
        } catch (InterruptedException ignored) {
            LOG.info("Solr index not checked (thread interrupted)");
        } catch (SolrServerException | IOException e) {
            LOG.error("Failed to check the Solr index", e);
        }
    }
}

