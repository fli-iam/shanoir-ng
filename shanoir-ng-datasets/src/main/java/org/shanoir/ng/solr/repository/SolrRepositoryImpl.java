/**
 * 
 */
package org.shanoir.ng.solr.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrFacet;
import org.shanoir.ng.utils.Range;
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
	private static final String DATASET_NAME_FACET = "datasetName";
	private static final String EXAMINATION_COMMENT_FACET = "examinationComment";
	private static final String SUBJECT_NAME_FACET = "subjectName";
	private static final String STUDY_NAME_FACET = "studyName";
	private static final String CENTER_NAME_FACET = "centerName";
    private static final String TAGS_FACET = "tags";

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
			criteria = criteria.and(Criteria.where(fieldName).is(values).setPartIsOr(true););
		}
	}
	
	
	private void addAndPredicateToCriteria(Criteria criteria, String fieldName, Range<Float> range) {
		if (range != null && (range.getLowerBound() != null || range.getUpperBound() != null)) {
			criteria = criteria.and(Criteria.where(fieldName).between(range.getLowerBound(), range.getUpperBound()));
		}
	}


	private SolrResultPage<ShanoirSolrDocument> getSearchResultsWithFacets(Criteria criteria, ShanoirSolrFacet facet, Pageable pageable) throws RestServiceException {
		addAndPredicateToCriteria(criteria, STUDY_NAME_FACET, facet.getStudyName());
		addAndPredicateToCriteria(criteria, SUBJECT_NAME_FACET, facet.getSubjectName());
		addAndPredicateToCriteria(criteria, EXAMINATION_COMMENT_FACET, facet.getExaminationComment());
		addAndPredicateToCriteria(criteria, DATASET_NAME_FACET, facet.getDatasetName());
		addAndPredicateToCriteria(criteria, DATASET_TYPE_FACET, facet.getDatasetType());
		addAndPredicateToCriteria(criteria, DATASET_NATURE_FACET, facet.getDatasetNature());
		addAndPredicateToCriteria(criteria, CENTER_NAME_FACET, facet.getCenterName());
		addAndPredicateToCriteria(criteria, TAGS_FACET, facet.getTags());
		addAndPredicateToCriteria(criteria, "sliceThickness", facet.getSliceThickness());
		addAndPredicateToCriteria(criteria, "pixelBandwidth", facet.getPixelBandwidth());
		addAndPredicateToCriteria(criteria, "magneticFieldStrength", facet.getMagneticFieldStrength());
		
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
						.addFacetOnField(TAGS_FACET)
						.setFacetLimit(-1));

		try {
			FacetPage<ShanoirSolrDocument> result = solrTemplate.queryForFacetPage("shanoir", query, ShanoirSolrDocument.class);			
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
			String[] fields = {STUDY_NAME_FACET, SUBJECT_NAME_FACET, DATASET_NAME_FACET, EXAMINATION_COMMENT_FACET, DATASET_TYPE_FACET, DATASET_NATURE_FACET, CENTER_NAME_FACET, TAGS_FACET};
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
