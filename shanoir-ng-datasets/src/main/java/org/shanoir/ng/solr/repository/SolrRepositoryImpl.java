/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.FacetParams;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.paging.FacetPageable;
import org.shanoir.ng.shared.paging.FacetPageable.FacetOrder;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.solr.config.SolrConfig;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrQuery;
import org.shanoir.ng.utils.KeycloakUtil;
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
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.SimpleFacetFieldEntry;
import org.springframework.data.solr.core.query.result.SolrResultPage;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

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
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteriaForAdmin(ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return getSearchResultsWithFacetsForAdmin(facet, pageable);			
		} else {
			throw new IllegalStateException("This method cannot be called by a non-admin user");
		}
	}

	@Override
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Map<Long, List<String>> studyIds,
			ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException {
		if (studyIds == null || studyIds.isEmpty()) {
			return new SolrResultPage<>(new ArrayList<>());
		} else {
			return getSearchResultsWithFacets(facet, pageable, studyIds);			
		}

	}

	private void addFilterQuery(SolrQuery query, String fieldName, Collection<String> values) {
		if (values != null && !values.isEmpty()) {
			query.addFilterQuery(
					"{!tag=" + fieldName + "}" 
					+ fieldName + ":(\"" + String.join("\" OR \"", values) + "\")");
		} 
	}

	/**
	 * For the given query in entry, add a filter by study center.
	 * Looks like 'AND ((study_id = X AND center_id IN (A, B, C)) OR (study_id = Y AND center_id IN (D, E)) OR study_id = Z)
	 * @param query
	 * @param studyIdCentersMap
	 */
	private void addFilterQueryForCenterStudy(SolrQuery query, Map<Long, List<String>> studyIdCentersMap) {
		String filter = "";
		for (Entry<Long, List<String>> entry : studyIdCentersMap.entrySet()) {
			if (!filter.equals("")) {
				filter+=" OR ";
			}
			if (CollectionUtils.isEmpty(entry.getValue())) {
				filter = filter + STUDY_ID_FACET + ":" + entry.getKey();
			} else {
				boolean first = true;
				for (String centerName: entry.getValue()) {
					filter =  filter + (!first ? " OR " : "") + " (" + STUDY_ID_FACET + ":" + entry.getKey() + " AND " + CENTER_NAME_FACET + ":\"" + centerName + "\")";
					first = false;
				}
			}
		}
		query.addFilterQuery(filter);
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
			String rangeQueryStr = fieldName + ":[" 
					+ (range.getLowerBound() != null ? ClientUtils.escapeQueryChars(range.getLowerBound().toString()) : "*")
					+ " TO "
					+ (range.getUpperBound() != null ? ClientUtils.escapeQueryChars(range.getUpperBound().toString()) : "*") 	
					+ "]";	
			query.addFilterQuery(rangeQueryStr);
		}
	}

	private void addFilterQueryFromDateRange(SolrQuery query, String fieldName, Range<LocalDate> range) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		if (range != null && (range.getLowerBound() != null || range.getUpperBound() != null)) {
			String rangeQueryStr = fieldName + ":[" 
					+ (range.getLowerBound() != null ? ClientUtils.escapeQueryChars(range.getLowerBound().plusDays(1).atStartOfDay().format(formatter)) : "*")
					+ " TO "
					+ (range.getUpperBound() != null ? ClientUtils.escapeQueryChars(range.getUpperBound().plusDays(1).atStartOfDay().format(formatter)) : "*") 	
					+ "]";	
			query.addFilterQuery(rangeQueryStr);
		}
	}

	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacetsForAdmin(ShanoirSolrQuery query, Pageable pageable) throws RestServiceException {
		return getSearchResultsWithFacets(query, pageable, null);
	}

	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(ShanoirSolrQuery shanoirQuery, Pageable pageable, Map<Long, List<String>> studyIds) throws RestServiceException {
		SolrClient client = solrConfig.solrClient();
		final SolrQuery query = new SolrQuery("*:*");

		/* add user's filtering */
		addUserFiltering(query, shanoirQuery);

		boolean hasRestrictions = false;
		if (!CollectionUtils.isEmpty(studyIds)) {
			for (Entry<Long, List<String>> element : studyIds.entrySet()) {
				if (!CollectionUtils.isEmpty(element.getValue())) {
					hasRestrictions = true;
				}
			}
			if (hasRestrictions) {
				addFilterQueryForCenterStudy(query, studyIds);
			} else {
				/* add study filtering */
				addFilterQueryFromLongs(query, STUDY_ID_FACET, studyIds.keySet());
			}
		}

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
			LOG.debug("Solr search : " + query);
			response = client.query("shanoir", query);
			LOG.debug("Solr response : " + response);
		} catch (IOException e) {
			throw new RestServiceException(e, new ErrorModel(500, "Error querying Solr"));
		} catch (SolrException | SolrServerException e) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "solr query failed");
			throw new RestServiceException(e, error);
		}

		/* build the page object */
		return buildShanoirSolrPage(response, pageable, shanoirQuery.getFacetPaging());
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
		addFilterQueryFromDateRange(query, DATASET_CREATION_DATE_FACET, shanoirQuery.getDatasetDateRange());

		if (shanoirQuery.getSearchText() != null && !shanoirQuery.getSearchText().trim().isEmpty()) {
			if (shanoirQuery.isExpertMode()) {
				addExpertClause(query, shanoirQuery.getSearchText());
			} else {
				addSearchInAllClause(query, shanoirQuery.getSearchText());
			}
		}
	}

	private SolrResultPage<ShanoirSolrDocument> buildShanoirSolrPage(QueryResponse response, Pageable pageable, Map<String, FacetPageable> facetPaging) {

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
					Page<FacetFieldEntry> facetPage = new PageImpl<FacetFieldEntry>(buildFacetResultPage(facetField, facetPaging.get(facetField.getName())));
					page.addFacetResultPage(facetPage, new SimpleField(facetField.getName()));
				}

			}			
		}
		return page;
	}

	private Page<FacetFieldEntry> buildFacetResultPage(FacetField facetField, FacetPageable facetPageable) {
		List<FacetFieldEntry> content = new ArrayList<>();
		for (FacetField.Count facetFieldCount : facetField.getValues()) {
			Field field = new SimpleField(facetField.getName());
			FacetFieldEntry facetFieldEntry = new SimpleFacetFieldEntry(
					field, 
					facetFieldCount.getName(), 
					facetFieldCount.getCount());
			content.add(facetFieldEntry);
		}

		Page<FacetFieldEntry> facetPage = new PageImpl<FacetFieldEntry>(content, facetPageable, facetField.getValueCount());
		return facetPage;
	}

	private void addExpertClause(SolrQuery query, String searchStr) {
		LOG.warn("Solr expert research : " + searchStr);
		//searchStr = searchStr.replace(STUDY_ID_FACET, "");
		query.addFilterQuery(searchStr);
	}

	private void addSearchInAllClause(SolrQuery query, String searchStr) {
		if (searchStr != null && !searchStr.isEmpty()) {
			String[] searchTerms = searchStr.trim().split(" ");		
			for (String term : searchTerms) {
				term = ClientUtils.escapeQueryChars(term);
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
				query.addFilterQuery(termInAnyFieldFormattedStr.toString());
			}
		}
	}

	private void addFacetPaging(SolrQuery query, ShanoirSolrQuery shanoirQuery) {
		if (shanoirQuery.getFacetPaging() != null) {
			query.setFacetMinCount(1);
			query.set("facet.numTerms", true);
			for (String facetName : TEXTUAL_FACET_LIST) {
				if (shanoirQuery.getFacetPaging().containsKey(facetName)) {
					query.addFacetField("{!ex=" + facetName + "}"  + facetName); // needed ?
					FacetPageable facetPageable = shanoirQuery.getFacetPaging().get(facetName);	
					query.set("f." + facetName + "." + FacetParams.FACET_LIMIT, facetPageable.getPageSize());
					query.set("f." + facetName + "." + FacetParams.FACET_OFFSET, (facetPageable.getPageNumber() - 1) * facetPageable.getPageSize());
					query.set("f." + facetName + ".numTerms", true);
					if (facetPageable.getFilter() != null && !facetPageable.getFilter().isEmpty()) {
						query.set("f." + facetName + "." + FacetParams.FACET_CONTAINS_IGNORE_CASE, true);
						query.set("f." + facetName + "." + FacetParams.FACET_CONTAINS, ClientUtils.escapeQueryChars(facetPageable.getFilter()).trim());
					}
					if (facetPageable.getFacetOrder() != null) {
						query.set("f." + facetName + "." + FacetParams.FACET_SORT, facetPageable.getFacetOrder().equals(FacetOrder.COUNT) ? "count" : "index");
					}
				}
			}
		}
	}

}
