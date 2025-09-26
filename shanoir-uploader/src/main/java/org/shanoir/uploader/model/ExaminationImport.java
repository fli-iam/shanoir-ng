package org.shanoir.uploader.model;



public class ExaminationImport {

    private String path;
    private String examName;
    private String subjectName;
    private FolderImport parent;
    private String message;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public FolderImport getParent() {
        return parent;
    }

    public void setParent(FolderImport parent) {
        this.parent = parent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the rawData
     */
    public String[] getRawData() {
        return new String[]{"" + this.parent.getStudy().getName(), "" + this.parent.getStudyCard().getName(), this.subjectName, this.examName, this.message};
    }

}
