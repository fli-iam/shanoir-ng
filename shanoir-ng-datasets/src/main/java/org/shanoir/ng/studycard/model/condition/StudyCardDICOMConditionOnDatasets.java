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

package org.shanoir.ng.studycard.model.condition;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@Component
@DiscriminatorValue("StudyCardDICOMConditionOnDatasets")
@JsonTypeName("StudyCardDICOMConditionOnDatasets")
public class StudyCardDICOMConditionOnDatasets extends StudyCardCondition implements ApplicationContextAware {
	
    private static final Logger LOG = LoggerFactory.getLogger(StudyCardDICOMConditionOnDatasets.class);
    
	private int dicomTag;

    private static ApplicationContext context;
	
	public Integer getDicomTag() {
        return dicomTag;
    }

    public void setDicomTag(Integer dicomTag) {
        this.dicomTag = dicomTag;
    }
    
    public boolean fulfilled(ExaminationAttributes<?> dicomAttributes) {
        return fulfilled(dicomAttributes, new StringBuffer());
    }

    public <T> boolean fulfilled(ExaminationAttributes<T> examinationAttributes, StringBuffer errorMsg) {
        if (examinationAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0; int total = 0; int nbUnknown = 0;
        for (T acqId : examinationAttributes.getAcquisitionIds()) {
            AcquisitionAttributes<T> acqAttributes = examinationAttributes.getAcquisitionAttributes(acqId);
            for (T datasetId : acqAttributes.getDatasetIds()) {
                total++;
                boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
                if (!alreadyFulfilled) {
                    Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg, datasetId );
                    if (fulfilled == null) {
                        nbUnknown++;
                    }
                    else if (fulfilled) {
                        nbOk++;
                    }
                }  
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(errorMsg, complies, nbOk, nbUnknown, total);
        return complies;
    }
    
    public boolean fulfilled(List<DatasetAcquisition> acquisitions, StringBuffer errorMsg) {
        if (acquisitions == null) throw new IllegalArgumentException("acquisitions can not be null");
        int nbOk = 0; int total = 0; int nbUnknown = 0;
        for (DatasetAcquisition acquisition : acquisitions) {
            for (Dataset dataset : acquisition.getDatasets()) {
                total++;
                boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
                if (!alreadyFulfilled) {
                    Boolean fulfilled = fulfilled(dataset, errorMsg);
                    if (fulfilled == null) {
                        nbUnknown++;
                    }
                    else if (fulfilled) {
                        nbOk++;
                    }
                }  
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(errorMsg, complies, nbOk, nbUnknown, total);
        return complies;
    }

    public boolean fulfilled(AcquisitionAttributes<?> acqAttributes) {
        return fulfilled(acqAttributes, new StringBuffer());
    }

    public <T> boolean fulfilled(AcquisitionAttributes<T> acqAttributes, StringBuffer errorMsg) {
        if (acqAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0; int total = 0; int nbUnknown = 0;
        for (T datasetId : acqAttributes.getDatasetIds()) {
            total++;
            boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
            if (!alreadyFulfilled) {
                Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg,datasetId);
                if (fulfilled == null) {
                    nbUnknown++;
                }
                else if (fulfilled) {
                    nbOk++;
                }
            }  
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(errorMsg, complies, nbOk, nbUnknown, total);
        return complies;
    }

    public Boolean fulfilled(Attributes dicomAttributes, Object datasetId) {
        return fulfilled(dicomAttributes, new StringBuffer(), datasetId);
    }
        
    private Boolean fulfilled(Attributes dicomAttributes, StringBuffer errorMsg, Object datasetId) {
        LOG.debug("conditionFulfilled: " + this.getId() + " processing condition " + getId() +  " with all its values: ");
        this.getValues().stream().forEach(s -> LOG.debug(s));
        if (dicomAttributes == null) {
            throw new IllegalArgumentException("dicomAttributes can't be null");
        }
        if (!dicomAttributes.contains(getDicomTag())) {
            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                + "] failed on dataset " + datasetId + " because no value was found in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
            return false;
        }
        VR tagVr = StandardElementDictionary.INSTANCE.vrOf(dicomTag);
        DicomTagType tagType = DicomTagType.valueOf(tagVr);
        // get all possible values, that can fulfill the condition
        for (String value : this.getValues()) {
            if (tagType.isNumerical()) {
                if (!this.getOperation().isNumerical()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not compatible with dicom tag " 
                            + this.getDicomTag() + " of type " + tagType + "(condition id : " + this.getId() + ")");
                } else {
                    BigDecimal scValue = new BigDecimal(value);
                    Integer comparison = null;
                    if (DicomTagType.Float.equals(tagType)) {
                        Float floatValue = dicomAttributes.getFloat(this.getDicomTag(), Float.NaN);
                        if (floatValue.equals(Float.NaN)) {
                            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                                + "] failed on dataset " + datasetId + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(floatValue).compareTo(scValue);
                    // There is no dicomAttributes.getLong() !
                    } else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
                        Double doubleValue = dicomAttributes.getDouble(this.getDicomTag(), Double.NaN);
                        if (doubleValue.equals(Double.NaN)) {
                            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                                + "] failed on dataset " + datasetId + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(doubleValue).compareTo(scValue);
                    } else if (DicomTagType.Integer.equals(tagType)) {
                        Integer integerValue = dicomAttributes.getInt(this.getDicomTag(), Integer.MIN_VALUE);
                        if (integerValue.equals(Integer.MIN_VALUE)) {
                            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                                + "] failed on dataset " + datasetId + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(integerValue).compareTo(scValue);
                    } else if (DicomTagType.Date.equals(tagType)) {
                        Date dateValue = dicomAttributes.getDate(this.getDicomTag());
                        if (dateValue.equals(null)) {
                            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                                + "] failed on dataset " + datasetId + " because could not find/extract a value in the dicom for the date tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else {
                            try {
                                Date scDate = new SimpleDateFormat("yyyyMMdd").parse(value);
                                comparison = dateValue.compareTo(scDate);
                            } catch (ParseException e) {
                                if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                                    + "] could not be checked on dataset " + datasetId + " because there was a date format problem");
                                return null;
                            }
                        }
                    } else {
                        throw new IllegalStateException("tagType for tag " + dicomTag + " is not implemented, tagType : " + tagType);
                    }
                    if (comparison != null && numericalCompare(this.getOperation(), comparison)) {
                        if (errorMsg != null) errorMsg.append("\ncondition [" + toString() + "] succeed on dataset " + datasetId + ", value found : " + dicomAttributes.getString(this.getDicomTag()));
                        return true; // as condition values are combined by OR: return if one is true
                    } // else continue to check other values
                }
            } else if (tagType.isTextual()) {
                if (!this.getOperation().isTextual()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not a textual operation and is not compatible with the textual dicom tag " 
                            + getDicomTagCodeAndLabel(this.getDicomTag()) + " of type " + tagType + "(condition id : " + this.getId() + ")");
                } else {
                    String stringValue = dicomAttributes.getString(this.getDicomTag());
                    if (stringValue == null) {
                        LOG.warn("Could not find a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                            + "] failed because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else if (textualCompare(this.getOperation(), stringValue, value)) {
                        if (errorMsg != null) errorMsg.append("\ncondition [" + toString() + "] succeed on acquisition ");
                        return true; // as condition values are combined by OR: return if one is true
                    } // else continue to check other values
                }
            } else  {
                throw new IllegalStateException("tagType for tag " + dicomTag + " is neither numerical or textual, tagType : " + tagType);
            }
        }
        if (errorMsg != null) errorMsg.append("\ncondition [" + toString() + "] failed on dataset " + datasetId + ", the found dicom value : " + dicomAttributes.getString(this.getDicomTag()) + " matches none of the given values : [" + String.join(", ", getValues()) + "] - operator : " + getOperation() + ")");
        return false;
    }


    private Boolean fulfilled(Dataset dataset, StringBuffer errorMsg) {
        try {
            WADODownloaderService downloader = (WADODownloaderService) context.getBean("WADODownloaderService");
            Attributes attributes = downloader.getDicomAttributesForDataset(dataset);
            return fulfilled(attributes, errorMsg, dataset.getId());
        } catch (PacsException e) {
            if (errorMsg != null) errorMsg.append("\ncondition [" + toString() 
                + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs");
            LOG.warn("condition [" + toString() 
                + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs, reason : " + e.getMessage());
            return null;
        }
    }

    private void writeConditionsReport(StringBuffer errorMsg, boolean complies, int nbOk, int nbUnknown, int total) {
         if (!complies) {
            if (getCardinality() == -1) {
                errorMsg.append("\ncondition [" + toString() + "] failed because only " + nbOk + " out of all (" + total + ") datasets complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
            } else if (getCardinality() == 0) {
                errorMsg.append("\ncondition [" + toString() + "] failed because " + nbOk + " datasets complied where 0 was required" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
            } else {
                errorMsg.append("\ncondition [" + toString() + "] failed because only " + nbOk + " out of " + total + " datasets complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
            }
        } else {
            errorMsg.append("\ncondition [" + toString() + "] succeed because " + nbOk + " out of " + total + " datasets complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getCardinality() == -1) {
                sb.append("all datasets ");
            } else if (getCardinality() == 0) {
                sb.append("no dataset ");
            } else {
                sb.append(getCardinality()).append(" datasets ");
            }
        sb.append("DICOM field ").append(getDicomTagCodeAndLabel(getDicomTag()))
            .append(" ").append(getOperation().name())
            .append(" to ")
            .append(StringUtils.join(getValues(), " or "));        
        return sb.toString();
    }
    
    private String getDicomTagHexString(int tag) {
        String hexStr = Integer.toHexString(tag);
        hexStr = StringUtils.leftPad(hexStr, 8, "0");
        hexStr = hexStr.substring(0, 5) + "," + hexStr.substring(5);
        return hexStr;
    }
    
    private String getDicomTagCodeAndLabel(int tag) {
        return Keyword.valueOf(tag) + " (" + getDicomTagHexString(tag) + ")";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;   
    }
}
