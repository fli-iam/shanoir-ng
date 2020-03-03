/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.Collection;

import javax.annotation.Resource;

import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrFacet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.FacetQuery;
import org.springframework.data.solr.core.query.Node;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.SolrResultPage;

/**
 * @author yyao
 *
 */
public class SolrRepositoryImpl implements SolrRepositoryCustom{
	@Resource
	private SolrTemplate solrTemplate;
	
	@Override
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteria(ShanoirSolrFacet facet, Pageable pageable) {
		Criteria criteria = new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD);
		return getSearchResultsWithFacets(criteria, facet, pageable);
	}
	
	@Override
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Collection<Long> studyIds, 
			ShanoirSolrFacet facet, Pageable pageable) {
		Criteria criteria = new Criteria("studyId").in(studyIds);
		return getSearchResultsWithFacets(criteria, facet, pageable);
		
	}
	
	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(Criteria criteria, ShanoirSolrFacet facet, Pageable pageable) {
		
		if (facet.getStudyName() != null && facet.getStudyName().size() > 0) {
			criteria.and(Criteria.where("studyName").in(facet.getStudyName()));
		}
		if (facet.getSubjectName() != null && facet.getSubjectName().size() > 0) {
			criteria.and(Criteria.where("subjectName").in(facet.getSubjectName()));
		}
		if (facet.getExaminationComment() != null && facet.getExaminationComment().size() > 0) {
			criteria.and(Criteria.where("examinationComment").in(facet.getExaminationComment()));
		}
		if (facet.getDatasetName() != null && facet.getDatasetName().size() > 0) {
			criteria.and(Criteria.where("datasetName").in(facet.getDatasetName()));
		}
		if (facet.getDatasetStartDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").greaterThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetStartDate())));
		}
		if (facet.getDatasetEndDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").lessThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetEndDate())));
		}
		if (facet.getDatasetType() != null && facet.getDatasetType().size() > 0) {
			criteria.and(Criteria.where("datasetType").in(facet.getDatasetType()));
		}
		if (facet.getDatasetNature() != null && facet.getDatasetNature().size() > 0) {
			criteria.and(Criteria.where("datasetNature").in(facet.getDatasetNature()));
		}
		
		criteria = combineCriteria(criteria);
		
		FacetQuery query = new SimpleFacetQuery(criteria)
		  .setFacetOptions(new FacetOptions().addFacetOnField("studyName_str").setFacetLimit(100)
		  .addFacetOnField("subjectName_str").setFacetLimit(100)
		  .addFacetOnField("examinationComment_str").setFacetLimit(100)
		  .addFacetOnField("datasetName_str").setFacetLimit(100)
		  .addFacetOnField("datasetType").setFacetLimit(100)
		  .addFacetOnField("datasetNature").setFacetLimit(100));
		FacetPage<ShanoirSolrDocument> facetPage = solrTemplate.queryForFacetPage(query, ShanoirSolrDocument.class);
		
		SolrResultPage<ShanoirSolrDocument> result = (SolrResultPage<ShanoirSolrDocument>) facetPage;
		return result;
	}
	
	private Criteria combineCriteria(Node node) {

		if (node.getParent() != null) {
			node = node.and(combineCriteria(node.getParent()));
		}
		
		return (Criteria) node;
	}
}
