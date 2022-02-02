/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.paging.FacetPageable;
import org.shanoir.ng.shared.paging.FacetPageable.FacetOrder;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.solr.config.SolrConfig;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrQuery;
import org.shanoir.ng.utils.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.SolrPageRequest;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.SimpleFacetFieldEntry;
import org.springframework.data.solr.core.query.result.SolrResultPage;

/**
 * @author yyao
 *
 */
public class SolrRepositoryImpl implements SolrRepositoryCustom {
	
	private static final Logger LOG = LoggerFactory.getLogger(SolrRepositoryImpl.class);
	
	private static final String DOCUMENT_ID_FACET = "id";
	private static final String DATASET_ID_FACET = "datasetId";
	private static final String DATASET_NAME_FACET = "datasetName";
	private static final String DATASET_TYPE_FACET = "datasetType";
	private static final String DATASET_NATURE_FACET = "datasetNature";
	private static final String DATASET_CREATION_DATE_FACET = "datasetCreationDate";
	private static final String EXAMINATION_COMMENT_FACET = "examinationComment";
	private static final String EXAMINATION_DATE_FACET = "examinationDate";
	private static final String SUBJECT_NAME_FACET = "subjectName";
	private static final String STUDY_NAME_FACET = "studyName";
	private static final String STUDY_ID_FACET = "studyId";
	private static final String CENTER_NAME_FACET = "centerName";
	private static final String SLICE_THICKNESS_FACET = "sliceThickness";
	private static final String PIXEL_BANDWIDTH_FACET = "pixelBandwidth";
	private static final String MAGNETIC_FIELD_STRENGHT_FACET = "magneticFieldStrength";
    private static final String TAGS_FACET = "tags";

    private static final String[] DOCUMENT_FACET_LIST = {
    		DOCUMENT_ID_FACET,
    		DATASET_ID_FACET,
    		DATASET_NAME_FACET,
    		DATASET_TYPE_FACET,
    		DATASET_NATURE_FACET,
    		DATASET_CREATION_DATE_FACET,
    		EXAMINATION_COMMENT_FACET,
    		EXAMINATION_DATE_FACET,
    		SUBJECT_NAME_FACET,
    		STUDY_NAME_FACET,
    		STUDY_ID_FACET,
    		CENTER_NAME_FACET,
    		SLICE_THICKNESS_FACET,
    		PIXEL_BANDWIDTH_FACET,
    		MAGNETIC_FIELD_STRENGHT_FACET,
    	    TAGS_FACET,	
    };

    private static final String[] TEXTUAL_FACET_LIST = {
    		DATASET_NAME_FACET,
    		DATASET_TYPE_FACET,
    		DATASET_NATURE_FACET,
    		EXAMINATION_COMMENT_FACET,
    		SUBJECT_NAME_FACET,
    		STUDY_NAME_FACET,
    		CENTER_NAME_FACET,
    	    TAGS_FACET,	
    };

	@Resource
	private SolrTemplate solrTemplate;
	
	@Autowired 
	private SolrConfig solrConfig;
	

	@Override
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteria(ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException {
		//return getSearchResultsWithFacets(criteria, facet, pageable);
		return getSearchResultsWithFacets(facet, pageable);
	}

	@Override
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Collection<Long> studyIds,
			ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException {
		return getSearchResultsWithFacets(facet, pageable, studyIds);

	}

	private void addFilterQuery(SolrQuery query, String fieldName, Collection<String> values) {
		if (values != null && !values.isEmpty()) {
			query.addFilterQuery(fieldName + ":(\"" + String.join("\" OR \"", values) + "\")");
		} 
	}
	
	private void addFilterQueryFromLongs(SolrQuery query, String fieldName, Collection<Long> values) {
		if (values != null && !values.isEmpty()) {
			List<String> valueStr = new ArrayList<>();
			for (Long longValue : values) {
				valueStr.add(longValue.toString());
			}
			addFilterQuery(query, fieldName, valueStr);			
		}
	}
	
	private <T> void addFilterQueryFromRange(SolrQuery query, String fieldName, Range<T> range) {
		if (range != null && (range.getLowerBound() != null || range.getUpperBound() != null)) {
			query.addFilterQuery(fieldName + ":[" 
					+ range.getLowerBound() != null ? range.getLowerBound().toString() : "*"
					+ ","
					+ range.getUpperBound() != null ? range.getUpperBound().toString() : "*"  	
					+ "]"
			);
		}
	}
	
	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(ShanoirSolrQuery query, Pageable pageable) throws RestServiceException {
		return getSearchResultsWithFacets(query, pageable, null);
	}
	
	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(ShanoirSolrQuery shanoirQuery, Pageable pageable, Collection<Long> studyIds) throws RestServiceException {
		SolrClient client = solrConfig.solrClient();
		final SolrQuery query = new SolrQuery("*:*");
		
		/* add user's filtering */
		addUserFiltering(query, shanoirQuery);
		
		/* add study filtering */
		addFilterQueryFromLongs(query, STUDY_ID_FACET, studyIds);
		
		/* add sorting */
		if (pageable.getSort() != null) {
			for (Sort.Order order : pageable.getSort()) {
				query.addSort(order.getProperty(), order.getDirection().equals(Direction.ASC) ? ORDER.asc : ORDER.desc);				
			}
		}
		
		/* add paging */
		query.setRows(pageable.getPageSize());
		query.setStart(pageable.getPageNumber() * pageable.getPageSize());
		
		/* results with all the columns */
		for (String fieldStr : DOCUMENT_FACET_LIST) {			
			query.addField(fieldStr);
		}
		
		/* configure the returned facet values */
		addFacetPaging(query, shanoirQuery);
		
		/* query Solr server */
		QueryResponse response;
		try {
			response = client.query("shanoir", query);
		} catch (SolrServerException | IOException e) {
			throw new RestServiceException(e, new ErrorModel(500, "Error querying Solr"));
		}

		/* build the page object */
		return buildShanoirSolrPage(response, pageable);
	}
	
	private void addUserFiltering(SolrQuery query, ShanoirSolrQuery shanoirQuery) {
		/* add user's filtering */
		addFilterQuery(query, STUDY_NAME_FACET, shanoirQuery.getStudyName());
		addFilterQuery(query, SUBJECT_NAME_FACET, shanoirQuery.getSubjectName());
		addFilterQuery(query, EXAMINATION_COMMENT_FACET, shanoirQuery.getExaminationComment());
		addFilterQuery(query, DATASET_NAME_FACET, shanoirQuery.getDatasetName());
		addFilterQuery(query, DATASET_TYPE_FACET, shanoirQuery.getDatasetType());
		addFilterQuery(query, DATASET_NATURE_FACET, shanoirQuery.getDatasetNature());
		addFilterQuery(query, CENTER_NAME_FACET, shanoirQuery.getCenterName());
		addFilterQuery(query, TAGS_FACET, shanoirQuery.getTags());
		addFilterQueryFromRange(query, SLICE_THICKNESS_FACET, shanoirQuery.getSliceThickness());
		addFilterQueryFromRange(query, PIXEL_BANDWIDTH_FACET, shanoirQuery.getPixelBandwidth());
		addFilterQueryFromRange(query, MAGNETIC_FIELD_STRENGHT_FACET, shanoirQuery.getMagneticFieldStrength());
		addFilterQueryFromRange(query, DATASET_CREATION_DATE_FACET, shanoirQuery.getDatasetDateRange());
		
		if (shanoirQuery.getSearchText() != null && !shanoirQuery.getSearchText().trim().isEmpty()) {
			if (shanoirQuery.isExpertMode()) {
				addExpertClause(query, shanoirQuery.getSearchText());
			} else {
				addSearchInAllClause(query, shanoirQuery.getSearchText());
			}
		}
	}

	private SolrResultPage<ShanoirSolrDocument> buildShanoirSolrPage(QueryResponse response, Pageable pageable) {
		
		SolrDocumentList documents = response.getResults();
		if (documents == null) documents = new SolrDocumentList();
		List<ShanoirSolrDocument> solrDocuments = new ArrayList<>();
		for(SolrDocument document : documents) {
			  ShanoirSolrDocument solrDoc = new ShanoirSolrDocument();
			  solrDoc.setId((String) document.getFirstValue("id"));
			  solrDoc.setDatasetId((Long) document.getFirstValue("datasetId"));
			  solrDoc.setDatasetName((String) document.getFirstValue("datasetName"));
			  solrDoc.setDatasetType((String) document.getFirstValue("datasetType"));
			  solrDoc.setDatasetNature((String) document.getFirstValue("datasetNature"));
			  solrDoc.setDatasetCreationDate((Date) document.getFirstValue("datasetCreationDate"));
			  solrDoc.setExaminationComment((String) document.getFirstValue("examinationComment"));
			  solrDoc.setExaminationDate((Date) document.getFirstValue("examinationDate"));
			  solrDoc.setSubjectName((String) document.getFirstValue("subjectName"));
			  solrDoc.setStudyName((String) document.getFirstValue("studyName"));
			  solrDoc.setStudyId((Long) document.getFirstValue("studyId"));
			  solrDoc.setCenterName((String) document.getFirstValue("centerName"));
			  solrDoc.setSliceThickness((Double) document.getFirstValue("sliceThickness"));
			  solrDoc.setPixelBandwidth((Double) document.getFirstValue("pixelBandwidth"));
			  solrDoc.setMagneticFieldStrength((Double) document.getFirstValue("magneticFieldStrength"));
			  solrDocuments.add(solrDoc);
		}
		SolrResultPage<ShanoirSolrDocument> page = new SolrResultPage<>(solrDocuments, pageable, documents.getNumFound(), null);

		if (response.getFacetFields() != null) {
			for (FacetField facetField : response.getFacetFields()) {
				if (facetField.getValueCount() > 0) {
					Page<FacetFieldEntry> facetPage = new PageImpl<FacetFieldEntry>(buildFacetResultPage(facetField));
					page.addFacetResultPage(facetPage, new SimpleField(facetField.getName()));
				}
				
			}			
		}
		return page;
	}
	
	private Page<FacetFieldEntry> buildFacetResultPage(FacetField facetField) {
		List<FacetFieldEntry> content = new ArrayList<>();
		for (FacetField.Count facetFieldCount : facetField.getValues()) {
			Field field = new SimpleField(facetFieldCount.getName());
			FacetFieldEntry facetFieldEntry = new SimpleFacetFieldEntry(
					field, 
					facetFieldCount.getName(), 
					facetFieldCount.getCount());
			content.add(facetFieldEntry);
		}
		
		Pageable fakePageable = new SolrPageRequest(3, 3);
		
		Page<FacetFieldEntry> facetPage = new PageImpl<FacetFieldEntry>(content, fakePageable, facetField.getValueCount());
		return facetPage;
	}
	
	private void addExpertClause(SolrQuery query, String searchStr) {
		LOG.warn("Solr expert research : " + searchStr);
		//searchStr = searchStr.replace(STUDY_ID_FACET, "");
		query.addFilterQuery(searchStr);
	}

	private void addSearchInAllClause(SolrQuery query, String searchStr) {
		if (searchStr != null && !searchStr.isEmpty()) {
			String[] searchTerms = ClientUtils.escapeQueryChars(searchStr).trim().split(" ");
			
			List<String> termInAnyFieldFormattedStrList = new ArrayList<>();
			for (String term : searchTerms) {
				List<String> termInFieldFormattedStrList = new ArrayList<>();
				for (String field : TEXTUAL_FACET_LIST) {
					StringBuilder termInField = new StringBuilder()
							.append("(")
							.append(field)
							.append(":*")
							.append(term)
							.append("*)");
					termInFieldFormattedStrList.add(termInField.toString());
				}
				StringBuilder termInAnyFieldFormattedStr = new StringBuilder()
						.append("(")
						.append(String.join(" OR ", termInFieldFormattedStrList))
						.append(")");
				termInAnyFieldFormattedStrList.add(termInAnyFieldFormattedStr.toString());
			}
			query.addFilterQuery(String.join(" AND ", termInAnyFieldFormattedStrList));
		}
	}
	
	private void addFacetPaging(SolrQuery query, ShanoirSolrQuery shanoirQuery) {
		if (shanoirQuery.getFacetPaging() != null) {
			query.setFacetMinCount(0);
			for (String facetName : TEXTUAL_FACET_LIST) {
				if (shanoirQuery.getFacetPaging().containsKey(facetName)) {
					query.addFacetField(facetName); // needed ?
					FacetPageable facetPageable = shanoirQuery.getFacetPaging().get(facetName);	
					query.set( "f." + facetName + "." + FacetParams.FACET_LIMIT, facetPageable.getPageSize());
					query.set( "f." + facetName + "." + FacetParams.FACET_OFFSET, facetPageable.getPageNumber() * facetPageable.getPageSize());
					if (facetPageable.getFilter() != null && !facetPageable.getFilter().isEmpty()) {
						query.set("f." + facetName + "." + FacetParams.FACET_CONTAINS_IGNORE_CASE, true);
						query.set("f." + facetName + "." + FacetParams.FACET_CONTAINS, ClientUtils.escapeQueryChars(facetPageable.getFilter()).trim());
					}
					if (facetPageable.getFacetOrder() != null) {;				
						query.set("f." + facetName + "." + FacetParams.FACET_SORT, facetPageable.getFacetOrder().equals(FacetOrder.COUNT) ? "count" : "index");
					}
				}
			}
		}
	}

}
