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
		if (facet.getStudyName() != null && !facet.getStudyName().isEmpty()) {
			for (String studyName: facet.getStudyName()) {
				if (studyName.startsWith("*")) {criteria.and(Criteria.where("studyName").endsWith((studyName.substring(1, studyName.length()))));}
				if (studyName.endsWith("*")) {criteria.and(Criteria.where("studyName").startsWith((studyName.substring(0, studyName.length() - 1))));}
				else if(!studyName.contains("*")) {criteria.and(Criteria.where("studyName").in(facet.getStudyName()));}
			}
		}
		if (facet.getSubjectName() != null && !facet.getSubjectName().isEmpty()) {
			for (String subjectName: facet.getSubjectName()) {
				if (subjectName.startsWith("*")) {criteria.and(Criteria.where("subjectName").endsWith((subjectName.substring(1, subjectName.length()))));}
				if (subjectName.endsWith("*")) {criteria.and(Criteria.where("subjectName").startsWith((subjectName.substring(0, subjectName.length() - 1))));}
				else if(!subjectName.contains("*")) {criteria.and(Criteria.where("subjectName").in(facet.getSubjectName()));}
			}
		}
		if (facet.getExaminationComment() != null && !facet.getExaminationComment().isEmpty()) {
			for (String examinationComment: facet.getExaminationComment()) {
				if (examinationComment.startsWith("*")) {criteria.and(Criteria.where("examinationComment").endsWith((examinationComment.substring(1, examinationComment.length()))));}
				if (examinationComment.endsWith("*")) {criteria.and(Criteria.where("examinationComment").startsWith((examinationComment.substring(0, examinationComment.length() - 1))));}
				else if(!examinationComment.contains("*")) {criteria.and(Criteria.where("examinationComment").in(facet.getExaminationComment()));}
			}
			
		}
		if (facet.getDatasetName() != null && !facet.getDatasetName().isEmpty()) {
			for (String datasetName: facet.getDatasetName()) {
				if (datasetName.startsWith("*")) {criteria.and(Criteria.where("datasetName").endsWith((datasetName.substring(1, datasetName.length()))));}
				if (datasetName.endsWith("*")) {criteria.and(Criteria.where("datasetName").startsWith((datasetName.substring(0, datasetName.length() - 1))));}
				else if(!datasetName.contains("*")) {criteria.and(Criteria.where("datasetName").in(facet.getDatasetName()));}
			}
		}
		if (facet.getDatasetStartDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").greaterThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetStartDate())));
		}
		if (facet.getDatasetEndDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").lessThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetEndDate())));
		}
		if (facet.getDatasetType() != null && !facet.getDatasetType().isEmpty()) {
			for (String datasetType: facet.getDatasetType()) {
				if (datasetType.startsWith("*")) {criteria.and(Criteria.where("datasetType").endsWith((datasetType.substring(1, datasetType.length()))));}
				if (datasetType.endsWith("*")) {criteria.and(Criteria.where("datasetType").startsWith((datasetType.substring(0, datasetType.length() - 1))));}
				else if(!datasetType.contains("*")) {criteria.and(Criteria.where("datasetType").in(facet.getDatasetType()));}
			}
		}
		if (facet.getDatasetNature() != null && !facet.getDatasetNature().isEmpty()) {
			for (String datasetNature: facet.getDatasetNature()) {
				if (datasetNature.startsWith("*")) {criteria.and(Criteria.where("datasetNature").endsWith((datasetNature.substring(1, datasetNature.length()))));}
				if (datasetNature.endsWith("*")) {criteria.and(Criteria.where("datasetNature").startsWith((datasetNature.substring(0, datasetNature.length() - 1))));}
				else if(!datasetNature.contains("*")) {criteria.and(Criteria.where("datasetNature").in(facet.getDatasetNature()));}
			}
		}
		
		criteria = combineCriteria(criteria);
		
		SimpleFacetQuery query = ((FacetQuery) new SimpleFacetQuery(criteria)
		  .setPageRequest(pageable))
		  .setFacetOptions(new FacetOptions().addFacetOnField("studyName_str")
		  .addFacetOnField("subjectName_str")
		  .addFacetOnField("datasetName_str")
		  .addFacetOnField("examinationComment_str")
		  .addFacetOnField("datasetType")
		  .addFacetOnField("datasetNature").setFacetLimit(200));
		
		SolrResultPage<ShanoirSolrDocument> result = (SolrResultPage<ShanoirSolrDocument>) solrTemplate.queryForPage(query, ShanoirSolrDocument.class);
		FacetPage<ShanoirSolrDocument> result2 = solrTemplate.queryForFacetPage(query, ShanoirSolrDocument.class);
		

		return (SolrResultPage<ShanoirSolrDocument>) result2;
	}
	
	private Criteria combineCriteria(Node node) {
		if (node.getParent() != null) {
			node = node.and(combineCriteria(node.getParent()));
		}
		
		return (Criteria) node;
	}
}
