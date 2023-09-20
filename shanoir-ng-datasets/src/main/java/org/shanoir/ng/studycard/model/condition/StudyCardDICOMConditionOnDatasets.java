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

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("StudyCardDICOMConditionOnDatasets")
@JsonTypeName("StudyCardDICOMConditionOnDatasets")
public class StudyCardDICOMConditionOnDatasets extends StudyCardCondition {
	
    private static final Logger LOG = LoggerFactory.getLogger(StudyCardDICOMConditionOnDatasets.class);
    
	private int dicomTag;

    private  DicomTagType tagType;
	
	public Integer getDicomTag() {
        return dicomTag;
    }

    public void setDicomTag(Integer dicomTag) {
        this.dicomTag = dicomTag;
        VR tagVr = StandardElementDictionary.INSTANCE.vrOf(dicomTag);
        tagType = DicomTagType.valueOf(tagVr);
    }
    
    public boolean fulfilled(ExaminationAttributes dicomAttributes) {
        return fulfilled(dicomAttributes, new StringBuffer());
    }

    public boolean fulfilled(ExaminationAttributes examinationAttributes, StringBuffer errorMsg) {
        if (examinationAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0; int total = 0;

        for (Long acqId : examinationAttributes.getAcquisitionIds()) {
            AcquisitionAttributes acqAttributes = examinationAttributes.getAcquisitionAttributes(acqId);
            for (long datasetId : acqAttributes.getDatasetIds()) {
                total++;
                if (fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg, datasetId )) {
                    nbOk++;
                }  
            }
        }
        boolean complies = cardinalityComplies(nbOk, total);
        if (!complies) {
            if (getCardinality() == -1) {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of all (" + total + ") datasets complied");
            } else if (getCardinality() == 0) {
                errorMsg.append("condition [" + toString() + "] failed because " + nbOk + " datasets complied where 0 was required");
            } else {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of " + total + " datasets complied");
            }
        } else {
            errorMsg.append("condition [" + toString() + "] succeed");
        }
        return complies;
    }

    public boolean fulfilled(AcquisitionAttributes acqAttributes) {
        return fulfilled(acqAttributes, new StringBuffer());
    }

    public boolean fulfilled(AcquisitionAttributes acqAttributes, StringBuffer errorMsg) {
        if (acqAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0; int total = 0;
        for (long datasetId : acqAttributes.getDatasetIds()) {
            total++;
            if (fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg,datasetId )) {
                nbOk++;
            }  
        }
        boolean complies = cardinalityComplies(nbOk, total);
        if (!complies) {
            if (getCardinality() == -1) {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of all (" + total + ") datasets complied");
            } else if (getCardinality() == 0) {
                errorMsg.append("condition [" + toString() + "] failed because " + nbOk + " datasets complied where 0 was required");
            } else {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of " + total + " datasets complied");
            }
        } else {
            errorMsg.append("condition [" + toString() + "] succeed");
        }
        return complies;
    }

    public boolean fulfilled(Attributes dicomAttributes, long datasetId) {
        return fulfilled(dicomAttributes, new StringBuffer(), datasetId);
    }
        
    public boolean fulfilled(Attributes dicomAttributes, StringBuffer errorMsg, long datasetId) {
        LOG.debug("conditionFulfilled: " + this.getId() + " processing condition " + getId() +  " with all its values: ");
        this.getValues().stream().forEach(s -> LOG.debug(s));
        if (dicomAttributes == null) {
            if (errorMsg != null) errorMsg.append("condition [" + toString() 
                + "] was ignored on dataset " + datasetId + " because no dicom data was provided");
            return true;
        }
        if (!dicomAttributes.contains(getDicomTag())) {
            if (errorMsg != null) errorMsg.append("condition [" + toString() 
                + "] failed on dataset " + datasetId + " because no value was found in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
            return false;
        }
        // get all possible values, that can fulfill the condition
        for (String value : this.getValues()) {
            if (tagType.isNumerical()) {
                if (!this.getOperation().isNumerical()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not compatible with dicom tag " 
                            + this.getDicomTag() + " of type " + tagType + "(condition id : " + this.getId() + ")");
                }
                BigDecimal scValue = new BigDecimal(value);
                Integer comparison = null;
                if (DicomTagType.Float.equals(tagType)) {
                    Float floatValue = dicomAttributes.getFloat(this.getDicomTag(), Float.NaN);
                    if (floatValue.equals(Float.NaN)) {
                        if (errorMsg != null) errorMsg.append("condition [" + toString() 
                            + "] failed on dataset " + datasetId + " because there was a problem when reading the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else comparison = BigDecimal.valueOf(floatValue).compareTo(scValue);
                // There is no dicomAttributes.getLong() !
                } else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
                    Double doubleValue = dicomAttributes.getDouble(this.getDicomTag(), Double.NaN);
                    if (doubleValue.equals(Double.NaN)) {
                        if (errorMsg != null) errorMsg.append("condition [" + toString() 
                            + "] failed on dataset " + datasetId + " because there was a problem when reading the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else comparison = BigDecimal.valueOf(doubleValue).compareTo(scValue);
                } else if (DicomTagType.Integer.equals(tagType)) {
                    Integer integerValue = dicomAttributes.getInt(this.getDicomTag(), Integer.MIN_VALUE);
                    if (integerValue.equals(Integer.MIN_VALUE)) {
                        if (errorMsg != null) errorMsg.append("condition [" + toString() 
                            + "] failed on dataset " + datasetId + " because there was a problem when reading the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else comparison = BigDecimal.valueOf(integerValue).compareTo(scValue);
                } else if (DicomTagType.Date.equals(tagType)) {
                    Date dateValue = dicomAttributes.getDate(this.getDicomTag());
                    if (dateValue.equals(null)) {
                        if (errorMsg != null) errorMsg.append("condition [" + toString() 
                            + "] failed on dataset " + datasetId + " because there was a problem when reading the date tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else {
                        try {
                            Date scDate = new SimpleDateFormat("yyyyMMdd").parse(value);
                            comparison = dateValue.compareTo(scDate);
                        } catch (ParseException e) {
                            if (errorMsg != null) errorMsg.append("condition [" + toString() 
                                + "] failed on dataset " + datasetId + " because there was a problem parsing the value as a date");
                            return false;
                        }
                    }
                }
                if (comparison != null && numericalCompare(this.getOperation(), comparison)) {
                    if (errorMsg != null) errorMsg.append("condition [" + toString() + "] succeed on dataset " + datasetId);
                    return true; // as condition values are combined by OR: return if one is true
                }
            } else if (tagType.isTextual()) {
                if (!this.getOperation().isTextual()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not compatible with dicom tag " 
                            + getDicomTagCodeAndLabel(this.getDicomTag()) + " of type " + tagType + "(condition id : " + this.getId() + ")");
                }   
                String stringValue = dicomAttributes.getString(this.getDicomTag());
                if (stringValue == null) {
                    LOG.warn("Could not find a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                    if (errorMsg != null) errorMsg.append("condition [" + toString() 
                        + "] failed because there was a problem when reading the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                    return false;
                }               
                if (textualCompare(this.getOperation(), stringValue, value)) {
                    if (errorMsg != null) errorMsg.append("condition [" + toString() + "] succeed on acquisition ");
                    return true; // as condition values are combined by OR: return if one is true
                }
            }
        }
        if (errorMsg != null) errorMsg.append("condition [" + toString() + "] failed on dataset " + datasetId);
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
}
