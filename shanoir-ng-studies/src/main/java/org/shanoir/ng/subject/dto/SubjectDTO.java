package  org.shanoir.ng.subject.dto;

import java.util.Date;
import java.util.List;


public class SubjectDTO {

    private Long id;

    private Date birthDate;

    private String languageHemisphericDominance;

    private String manualHemisphericDominance;

    private String name;

    private String imagedObjectCategory;

    private String sex;

    private String subjectIdentifier;

//    private PseudonymusHashValues pseudonymusHashValues;

//    private List<Dataset> datasetList = new ArrayList<Dataset>(0);
//
//    private List<Examination> examinationList = new ArrayList<Examination>(0);
//
//    private List<RelSubjectGroupOfSubjects> relSubjectGroupOfSubjectsList = new ArrayList<RelSubjectGroupOfSubjects>(0);
//
    private List<SubjectStudyDTO> subjectStudyList;
//
//    private List<UserPersonalCommentSubject> userPersonalCommentList = new ArrayList<UserPersonalCommentSubject>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

//    public List<Dataset> getDatasetList() {
//        return datasetList;
//    }
//
//    public void setDatasetList(List<Dataset> datasetList) {
//        this.datasetList = datasetList;
//    }
//
//    public List<Examination> getExaminationList() {
//        return examinationList;
//    }
//
//    public void setExaminationList(List<Examination> examinationList) {
//        this.examinationList = examinationList;
//    }

   public String getLanguageHemisphericDominance() {
       return languageHemisphericDominance;
   }

   public void setLanguageHemisphericDominance(String languageHemisphericDominance) {
       this.languageHemisphericDominance = languageHemisphericDominance;
   }

   public String getManualHemisphericDominance() {
       return manualHemisphericDominance;
   }

   public void setManualHemisphericDominance(String manualHemisphericDominance) {
       this.manualHemisphericDominance = manualHemisphericDominance;
   }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   public String getImagedObjectCategory() {
       return imagedObjectCategory;
   }

   public void setImagedObjectCategory(String imagedObjectCategory) {
       this.imagedObjectCategory = imagedObjectCategory;
   }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

//    public List<RelSubjectGroupOfSubjects> getRelSubjectGroupOfSubjectsList() {
//        return relSubjectGroupOfSubjectsList;
//    }
//
//    public void setRelSubjectGroupOfSubjectsList(List<RelSubjectGroupOfSubjects> relSubjectGroupOfSubjectsList) {
//        this.relSubjectGroupOfSubjectsList = relSubjectGroupOfSubjectsList;
//    }
//
    public List<SubjectStudyDTO> getSubjectStudyList() {
        return subjectStudyList;
    }

    public void setSubjectStudyList(List<SubjectStudyDTO> subjectStudyList) {
        this.subjectStudyList = subjectStudyList;
    }

    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }

    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

//    public List<UserPersonalCommentSubject> getUserPersonalCommentList() {
//        return userPersonalCommentList;
//    }
//
//    public void setUserPersonalCommentList(List<UserPersonalCommentSubject> userPersonalCommentList) {
//        this.userPersonalCommentList = userPersonalCommentList;
//    }

//    public PseudonymusHashValues getPseudonymusHashValues() {
//        return pseudonymusHashValues;
//    }
//
//    public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
//        this.pseudonymusHashValues = pseudonymusHashValues;
//    }
//
}
