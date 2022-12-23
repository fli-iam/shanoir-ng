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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("StudyCardDICOMCondition")
public class StudyCardDICOMCondition extends StudyCardCondition {
	
    private static final Logger LOG = LoggerFactory.getLogger(StudyCardDICOMCondition.class);
    
	private int dicomTag;
	
	public Integer getDicomTag() {
        return dicomTag;
    }

    public void setDicomTag(Integer dicomTag) {
        this.dicomTag = dicomTag;
    }
    
    public boolean fulfilled(Attributes dicomAttributes) {
        return fulfilled(dicomAttributes, null);
    }
        
    public boolean fulfilled(Attributes dicomAttributes, String errorMsg) {
        LOG.info("conditionFulfilled: " + this.getId() + " processing one condition with all its values: ");
        this.getValues().stream().forEach(s -> LOG.info(s.getValue()));
        VR tagVr = StandardElementDictionary.INSTANCE.vrOf(this.getDicomTag());
        DicomTagType tagType = DicomTagType.valueOf(tagVr);
        // get all possible values, that can fulfill the condition
        for (StudyCardConditionValue value : this.getValues()) {
            if (value.getValue() == null) throw new IllegalArgumentException("A condition value cannot be null.");
            if (tagType.isNumerical()) {
                if (!this.getOperation().isNumerical()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not compatible with dicom tag " 
                            + this.getDicomTag() + " of type " + tagType + "(condition id : " + this.getId() + ")");
                }
                BigDecimal scValue = new BigDecimal(value.getValue());
                Integer comparison = null;
                if (DicomTagType.Float.equals(tagType)) {
                    Float floatValue = dicomAttributes.getFloat(this.getDicomTag(), Float.MIN_VALUE);          
                    comparison = BigDecimal.valueOf(floatValue).compareTo(scValue);
                // There is no dicomAttributes.getLong() !
                }   else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
                    Double doubleValue = dicomAttributes.getDouble(this.getDicomTag(), Double.MIN_VALUE);          
                    comparison = BigDecimal.valueOf(doubleValue).compareTo(scValue);
                } else if (DicomTagType.Integer.equals(tagType)) {
                    Integer integerValue = dicomAttributes.getInt(this.getDicomTag(), Integer.MIN_VALUE);
                    comparison = BigDecimal.valueOf(integerValue).compareTo(scValue);
                }
                if (comparison != null && numericalCompare(this.getOperation(), comparison)) {
                    return true; // as condition values are combined by OR: return if one is true
                }
            } else if (tagType.isTextual()) {
                if (!this.getOperation().isTextual()) {
                    throw new IllegalArgumentException("Study card processing : operation " + this.getOperation() + " is not compatible with dicom tag " 
                            + this.getDicomTag() + " of type " + tagType + "(condition id : " + this.getId() + ")");
                }   
                String stringValue = dicomAttributes.getString(this.getDicomTag());
                if (stringValue == null) {
                    LOG.warn("Could not find a value in the dicom for the tag " + this.getDicomTag());
                    return false;
                }               
                if (textualCompare(this.getOperation(), stringValue, value.getValue())) {
                    return true; // as condition values are combined by OR: return if one is true
                }
            }
        }
        return false;
    }
}
