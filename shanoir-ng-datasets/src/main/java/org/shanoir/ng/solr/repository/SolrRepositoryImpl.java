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
	private static final String DATASET_NATURE = "datasetNature";
	private static final String DATASET_TYPE = "datasetType";
	private static final String WILDCARD = "*";
	private static final String DATASET_NAME = "datasetName_str";
	private static final String EXAMINATION_COMMENT = "examinationComment";
	private static final String SUBJECT_NAME = "subjectName_str";
	private static final String STUDY_NAME = "studyName_str";
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
				if(!studyName.contains(WILDCARD)) {
					criteria.and(Criteria.where(STUDY_NAME).is(facet.getStudyName()));
				} else if (studyName.startsWith(WILDCARD) && studyName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(STUDY_NAME).contains(studyName.substring(1, studyName.length() -1)));
				} else if (studyName.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(STUDY_NAME).endsWith(studyName.substring(1, studyName.length())));
				} else if (studyName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(STUDY_NAME).startsWith(studyName.substring(0, studyName.length() - 1)));
				}
			}
		}
		if (facet.getSubjectName() != null && !facet.getSubjectName().isEmpty()) {
			for (String subjectName: facet.getSubjectName()) {
				if(!subjectName.contains(WILDCARD)) {
					criteria.and(Criteria.where(SUBJECT_NAME).is(facet.getSubjectName()));
				} else if (subjectName.startsWith(WILDCARD) && subjectName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(SUBJECT_NAME).contains(subjectName.substring(1, subjectName.length() -1)));
				} else if (subjectName.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(SUBJECT_NAME).endsWith(subjectName.substring(1, subjectName.length())));
				} else if (subjectName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(SUBJECT_NAME).startsWith(subjectName.substring(0, subjectName.length() - 1)));
				}
			}
		}
		if (facet.getExaminationComment() != null && !facet.getExaminationComment().isEmpty()) {
			for (String examinationComment: facet.getExaminationComment()) {
				if(!examinationComment.contains(WILDCARD)) {
					criteria.and(Criteria.where(EXAMINATION_COMMENT).is(facet.getExaminationComment()));
				} else if (examinationComment.startsWith(WILDCARD) && examinationComment.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(EXAMINATION_COMMENT).contains(examinationComment.substring(1, examinationComment.length() -1)));
				} else if (examinationComment.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(EXAMINATION_COMMENT).endsWith(examinationComment.substring(1, examinationComment.length())));
				} else if (examinationComment.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(EXAMINATION_COMMENT).startsWith(examinationComment.substring(0, examinationComment.length() - 1)));
				}
			}

		}
		if (facet.getDatasetName() != null && !facet.getDatasetName().isEmpty()) {
			for (String datasetName: facet.getDatasetName()) {
				if(!datasetName.contains(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NAME).is(facet.getDatasetName()));
				} else if (datasetName.startsWith(WILDCARD) && datasetName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NAME).contains(datasetName.substring(1, datasetName.length() -1)));
				} else if (datasetName.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NAME).endsWith(datasetName.substring(1, datasetName.length())));
				} else if (datasetName.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NAME).startsWith(datasetName.substring(0, datasetName.length() - 1)));
				}
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
				if(!datasetType.contains(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_TYPE).is(facet.getDatasetType()));
				} else if (datasetType.startsWith(WILDCARD) && datasetType.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_TYPE).contains(datasetType.substring(1, datasetType.length() -1)));
				} else if (datasetType.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_TYPE).endsWith(datasetType.substring(1, datasetType.length())));
				} else if (datasetType.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_TYPE).startsWith(datasetType.substring(0, datasetType.length() - 1)));
				}
			}
		}
		if (facet.getDatasetNature() != null && !facet.getDatasetNature().isEmpty()) {
			for (String datasetNature: facet.getDatasetNature()) {
				if(!datasetNature.contains(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NATURE).is(facet.getDatasetNature()));
				} else if (datasetNature.startsWith(WILDCARD) && datasetNature.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NATURE).contains(datasetNature.substring(1, datasetNature.length() -1)));
				} else if (datasetNature.startsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NATURE).endsWith(datasetNature.substring(1, datasetNature.length())));
				} else if (datasetNature.endsWith(WILDCARD)) {
					criteria.and(Criteria.where(DATASET_NATURE).startsWith(datasetNature.substring(0, datasetNature.length() - 1)));
				}
			}
		}

		criteria = combineCriteria(criteria);

		SimpleFacetQuery query = ((FacetQuery) new SimpleFacetQuery(criteria)
				.setPageRequest(pageable))
				.setFacetOptions(new FacetOptions().addFacetOnField("studyName_str")
						.addFacetOnField("subjectName_str")
						.addFacetOnField("datasetName_str")
						.addFacetOnField("examinationComment_str")
						.addFacetOnField(DATASET_TYPE)
						.addFacetOnField(DATASET_NATURE)
						.setFacetLimit(-1));

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
