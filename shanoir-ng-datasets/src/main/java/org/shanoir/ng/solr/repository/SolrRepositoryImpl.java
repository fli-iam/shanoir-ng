/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrFacet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.FacetQuery;
import org.springframework.data.solr.core.query.Node;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.SolrResultPage;
import org.springframework.http.HttpStatus;

/**
 * @author yyao
 *
 */
public class SolrRepositoryImpl implements SolrRepositoryCustom {
	private static final String DATASET_NATURE_FACET = "datasetNature";
	private static final String DATASET_TYPE_FACET = "datasetType";
	private static final String DATASET_NAME_FACET = "datasetName_str";
	private static final String EXAMINATION_COMMENT_FACET = "examinationComment_str";
	private static final String SUBJECT_NAME_FACET = "subjectName_str";
	private static final String STUDY_NAME_FACET = "studyName_str";
	private static final String CENTER_NAME_FACET = "centerName_str";
	@Resource
	private SolrTemplate solrTemplate;

	@Override
	public SolrResultPage<ShanoirSolrDocument> findByFacetCriteria(ShanoirSolrFacet facet, Pageable pageable) throws RestServiceException {
		Criteria criteria = new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD);
		return getSearchResultsWithFacets(criteria, facet, pageable);
	}

	@Override
	public SolrResultPage<ShanoirSolrDocument> findByStudyIdInAndFacetCriteria(Collection<Long> studyIds,
			ShanoirSolrFacet facet, Pageable pageable) throws RestServiceException {
		Criteria criteria = new Criteria("studyId").in(studyIds);
		return getSearchResultsWithFacets(criteria, facet, pageable);

	}
	
	private void addAndPredicateToCriteria(Criteria criteria, String fieldName, Collection<String> values) {
		if (values != null && !values.isEmpty()) {
			criteria = criteria.and(Criteria.where(fieldName).is(values));
		}
	}

	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(Criteria criteria, ShanoirSolrFacet facet, Pageable pageable) throws RestServiceException {
		addAndPredicateToCriteria(criteria, "studyName", facet.getStudyName());
		addAndPredicateToCriteria(criteria, "subjectName", facet.getSubjectName());
		addAndPredicateToCriteria(criteria, "examinationComment", facet.getExaminationComment());
		addAndPredicateToCriteria(criteria, "datasetName", facet.getDatasetName());
		addAndPredicateToCriteria(criteria, "datasetType", facet.getDatasetType());
		addAndPredicateToCriteria(criteria, "datasetNature", facet.getDatasetNature());
		
		if (facet.getDatasetStartDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").greaterThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetStartDate())));
		}
		if (facet.getDatasetEndDate() != null) {
			criteria.and(Criteria.where("datasetCreationDate").lessThanEqual(DateTimeUtils.localDateToSolrString(facet.getDatasetEndDate())));
		}
		
		if (facet.getSearchText() != null && !facet.getSearchText().trim().isEmpty()) {
			if (facet.isExpertMode()) {
				addExpertClause(criteria, facet.getSearchText());	
			} else {
				addSearchInAllClause(criteria, facet.getSearchText());			
			}			
		}

		criteria = combineCriteria(criteria);

		SimpleFacetQuery query = ((FacetQuery) new SimpleFacetQuery(criteria)
				.setPageRequest(pageable))
				.setFacetOptions(new FacetOptions()
						.addFacetOnField(STUDY_NAME_FACET)
						.addFacetOnField(SUBJECT_NAME_FACET)
						.addFacetOnField(DATASET_NAME_FACET)
						.addFacetOnField(EXAMINATION_COMMENT_FACET)
						.addFacetOnField(DATASET_TYPE_FACET)
						.addFacetOnField(DATASET_NATURE_FACET)
						.addFacetOnField(CENTER_NAME_FACET)
						.setFacetLimit(-1));

		try {
			FacetPage<ShanoirSolrDocument> result = solrTemplate.queryForFacetPage(query, ShanoirSolrDocument.class);			
			return (SolrResultPage<ShanoirSolrDocument>) result;
		} catch (UncategorizedSolrException e) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "solr query failed");
			throw new RestServiceException(e, error);
		}
	}
	
	private void addExpertClause(Criteria criteria, String searchStr) {
		criteria.and(new Criteria().expression(
				new StringBuilder("(").append(searchStr).append(")").toString()));
	}
	
	private void addSearchInAllClause(Criteria criteria, String searchStr) {
		if (searchStr != null && !searchStr.isEmpty()) {
			String[] fields = {"studyName", "subjectName", "datasetName", "examinationComment", "datasetType", "datasetNature", "centerName"};
			String[] specialChars = {"+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "/"};
			String escapedSearchStr = searchStr;
			for (String specialChar : specialChars) {
				escapedSearchStr = escapedSearchStr.replace(specialChar, '\\' + specialChar);
			}
			String[] searchTerms = escapedSearchStr.trim().split(" "); 
			
			List<String> termInAnyFieldFormattedStrList = new ArrayList<>();
			for (String term : searchTerms) {
				List<String> termInFieldFormattedStrList = new ArrayList<>();
				for (String field : fields) {
					StringBuilder termInField = new StringBuilder()
							.append("(")
							.append(field)
							.append(":")
							.append(Criteria.WILDCARD)
							.append(term)
							.append(Criteria.WILDCARD)
							.append(")");
					termInFieldFormattedStrList.add(termInField.toString());
				}
				StringBuilder termInAnyFieldFormattedStr = new StringBuilder()
						.append("(")
						.append(String.join(" OR ", termInFieldFormattedStrList))
						.append(")");
				termInAnyFieldFormattedStrList.add(termInAnyFieldFormattedStr.toString());
			}
			addExpertClause(criteria, String.join(" AND ", termInAnyFieldFormattedStrList));
		}
	}

	private Criteria combineCriteria(Node node) {
		if (node.getParent() != null) {
			node = node.and(combineCriteria(node.getParent()));
		}
		return (Criteria) node;
	}
}
