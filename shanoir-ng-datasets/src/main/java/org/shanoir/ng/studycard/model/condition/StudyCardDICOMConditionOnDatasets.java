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
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.shanoir.ng.studycard.model.Operation;
import org.shanoir.ng.studycard.model.VM;
import org.shanoir.ng.utils.Utils;
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

    public Integer getDicomTag() {
        return dicomTag;
    }

    public void setDicomTag(Integer dicomTag) {
        this.dicomTag = dicomTag;
    }

    public boolean fulfilled(ExaminationAttributes<?> dicomAttributes) {
        return fulfilled(dicomAttributes, new StringBuffer());
    }

    /**
     * Check the conditions on a complete set of already known dicom Attributes
     * @param <T> the type of the used keys
     * @param examinationAttributes complete set of already known dicom Attributes, not a cache
     * @param errorMsg
     * @return
     */
    public <T> boolean fulfilled(ExaminationAttributes<T> examinationAttributes, StringBuffer errorMsg) {
        if (examinationAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (T acqId : examinationAttributes.getAcquisitionIds()) {
            AcquisitionAttributes<T> acqAttributes = examinationAttributes.getAcquisitionAttributes(acqId);
            for (T datasetId : acqAttributes.getDatasetIds()) {
                total++;
                boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
                if (!alreadyFulfilled) {
                    Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg);
                    if (fulfilled == null) {
                        nbUnknown++;
                    } else if (fulfilled) {
                        nbOk++;
                    }
                }
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(errorMsg, complies, nbOk, nbUnknown, total);
        return complies;
    }

    /**
     * Check condition on acquisitions
     * @param acquisitions data checked
     * @param examinationAttributesCache to be used as a cache
     * @param errorMsg
     * @return
     */
    public boolean fulfilled(List<DatasetAcquisition> acquisitions, ExaminationAttributes<Long> examinationAttributesCache, WADODownloaderService downloader, StringBuffer errorMsg) {
        if (acquisitions == null) throw new IllegalArgumentException("acquisitions can not be null");
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (DatasetAcquisition acquisition : acquisitions) {
            if (!examinationAttributesCache.has(acquisition.getId())) {
                examinationAttributesCache.addAcquisitionAttributes(acquisition.getId(), new AcquisitionAttributes<Long>());
            }
            AcquisitionAttributes<Long> acqAttributes = examinationAttributesCache.getAcquisitionAttributes(acquisition.getId());
            for (Dataset dataset : acquisition.getDatasets()) {
                total++;
                boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
                if (!alreadyFulfilled) {
                    if (!acqAttributes.has(dataset.getId())) {
                        acqAttributes.addDatasetAttributes(dataset.getId(), downloadAttributes(dataset, downloader, errorMsg));
                    }
                    if (acqAttributes.getDatasetAttributes(dataset.getId()) == null) { // in case of pacs error
                        nbUnknown++;
                    } else {
                        Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(dataset.getId()), errorMsg);
                        if (fulfilled == null) {
                            nbUnknown++;
                        } else if (fulfilled) {
                            nbOk++;
                        }
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
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (T datasetId : acqAttributes.getDatasetIds()) {
            total++;
            boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
            if (!alreadyFulfilled) {
                Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(datasetId), errorMsg);
                if (fulfilled == null) {
                    nbUnknown++;
                } else if (fulfilled) {
                    nbOk++;
                }
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(errorMsg, complies, nbOk, nbUnknown, total);
        return complies;
    }

    public Boolean fulfilled(Attributes dicomAttributes) {
        return fulfilled(dicomAttributes, new StringBuffer());
    }

    private Boolean fulfilled(Attributes dicomAttributes, StringBuffer errorMsg) {
        LOG.debug("conditionFulfilled: " + this.getId() + " processing condition " + getId() +  " with all its values: ");
        this.getValues().stream().forEach(s -> LOG.debug(s));

        if (dicomAttributes == null) {
            throw new IllegalArgumentException("dicomAttributes can't be null");
        }
        VR tagVr = StandardElementDictionary.INSTANCE.vrOf(dicomTag);
        VM tagVm = VM.of(dicomTag);
        DicomTagType tagType = DicomTagType.valueOf(tagVr, tagVm);
        if (!this.getOperation().compatibleWith(tagType)) {
            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                    + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                    + " because the operation " + this.getOperation() + " is not compatible with dicom tag "
                    + this.getDicomTag() + " of type " + tagType + "(condition id : " + this.getId() + ")");
            return false;
        }

        if (Operation.PRESENT.equals(getOperation())) {
            if (dicomAttributes.contains(getDicomTag())) {
                return true;
            } else {
                if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                    + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                    + " because the tag " + getDicomTagCodeAndLabel(this.getDicomTag()) + " was required but was absent");
                return false;
            }
        } else if (Operation.ABSENT.equals(getOperation())) {
            if (dicomAttributes.contains(getDicomTag())) {
                if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                    + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                    + " because the tag " + getDicomTagCodeAndLabel(this.getDicomTag()) + " was required absent but was present");
                return false;
            } else {
                return true;
            }
        } else { // other operators

            if (!dicomAttributes.contains(getDicomTag())) {
                if (Utils.buildArrayList(
                        Operation.DOES_NOT_CONTAIN,
                        Operation.DOES_NOT_END_WITH,
                        Operation.DOES_NOT_START_WITH,
                        Operation.NOT_EQUALS).contains(getOperation())) {
                    if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                        + "] succeed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                        + " because no value found in the dicom for the tag : " + getDicomTagCodeAndLabel(this.getDicomTag()));
                    return true;
                } else {
                    if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                        + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                        + " because no value was found in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                    return false;
                }
            }

            // get all possible values, that can fulfill the condition
            for (String value : this.getValues()) {
                if (DicomTagType.String.equals(tagType)) {
                    String stringValue = dicomAttributes.getString(this.getDicomTag());
                    if (stringValue == null) {
                        LOG.warn("Could not find a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                            + "] failed because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else if (textualCompare(this.getOperation(), stringValue, value)) {
                        if (errorMsg != null) errorMsg.append("\nThe condition [" + toString() + "] succeed on acquisition ");
                        return true; // as condition values are combined by OR: return if one is true
                    } // else continue to check other values

                } else if (DicomTagType.FloatArray.equals(tagType)) {
                    float[] floatValues = dicomAttributes.getFloats(this.getDicomTag());
                    if (floatValues == null) {
                        if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                            + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                            + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                        return false;
                    } else {
                        float[] valueArr = extractFloatArray(value);
                        return arrayCompare(getOperation(), floatValues, valueArr);
                    }
                } else { // numerical simple comparisons
                    BigDecimal scValue = new BigDecimal(value);
                    Integer comparison = null;
                    if (DicomTagType.Float.equals(tagType)) {
                        Float floatValue = dicomAttributes.getFloat(this.getDicomTag(), Float.NaN);
                        if (floatValue.equals(Float.NaN)) {
                            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                                + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                                + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(floatValue).compareTo(scValue);
                    // There is no dicomAttributes.getLong() !
                    } else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
                        Double doubleValue = dicomAttributes.getDouble(this.getDicomTag(), Double.NaN);
                        if (doubleValue.equals(Double.NaN)) {
                            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                                + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                                + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(doubleValue).compareTo(scValue);
                    } else if (DicomTagType.Integer.equals(tagType)) {
                        Integer integerValue = dicomAttributes.getInt(this.getDicomTag(), Integer.MIN_VALUE);
                        if (integerValue.equals(Integer.MIN_VALUE)) {
                            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                                + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                                + " because could not find/extract a value in the dicom for the tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else comparison = BigDecimal.valueOf(integerValue).compareTo(scValue);
                    } else if (DicomTagType.Date.equals(tagType)) {
                        Date dateValue = dicomAttributes.getDate(this.getDicomTag());
                        if (dateValue.equals(null)) {
                            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                                + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                                + " because could not find/extract a value in the dicom for the date tag " + getDicomTagCodeAndLabel(this.getDicomTag()));
                            return false;
                        } else {
                            try {
                                Date scDate = new SimpleDateFormat("yyyyMMdd").parse(value);
                                comparison = dateValue.compareTo(scDate);
                            } catch (ParseException e) {
                                if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                                    + "] could not be checked on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber)
                                    + " because there was a date format problem (please use yyyyMMdd)");
                                return null;
                            }
                        }
                    } else {
                        throw new IllegalStateException("tagType for tag " + dicomTag + " is not implemented, tagType : " + tagType);
                    }
                    if (comparison != null && numericalCompare(this.getOperation(), comparison)) {
                        if (errorMsg != null) errorMsg.append("\nThe condition [" + toString() + "] succeed on dataset " + dicomAttributes.getString(Tag.SeriesDescription)
                        + " with DICOM seriesNumber " + dicomAttributes.getString(Tag.SeriesNumber) + ", value found : " + dicomAttributes.getString(this.getDicomTag()));
                        return true; // as condition values are combined by OR: return if one is true
                    } // else continue to check other values
                }
            }
        }

        if (errorMsg != null) errorMsg.append("\nThe condition [" + toString() + "] failed on dataset " + dicomAttributes.getString(Tag.SeriesDescription) + " with DICOM seriesNumber "
        + dicomAttributes.getString(Tag.SeriesNumber) + ", the found dicom value : " + dicomAttributes.getString(this.getDicomTag()) + " matches none of the given values : ["
        + String.join(", ", getValues()) + "] - operator : " + getOperation() + ")");
        return false;
    }

    private Attributes downloadAttributes(Dataset dataset, WADODownloaderService downloader, StringBuffer errorMsg) {
        try {
            Attributes attributes = downloader.getDicomAttributesForDataset(dataset);
            return attributes;
        } catch (PacsException e) {
            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                    + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs");
            LOG.warn("The condition [" + toString()
                    + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs, reason : " + e.getMessage());
            return null;
        }
    }

    private void writeConditionsReport(StringBuffer errorMsg, boolean complies, int nbOk, int nbUnknown, int total) {
        if (!complies) {
            switch (getCardinality()) {
                case -1 -> errorMsg.append("\nThe condition [" + toString() + "] failed because only " + nbOk + " out of all (" + total + ") dataset(s) complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
                case 0 -> errorMsg.append("\nThe condition [" + toString() + "] failed because " + nbOk + " dataset(s) complied where 0 was required" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
                default -> errorMsg.append("\nThe condition [" + toString() + "] failed because only " + nbOk + " out of " + total + " dataset(s) complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
            }
        } else {
            errorMsg.append("\nThe condition [" + toString() + "] succeed because " + nbOk + " out of " + total + " dataset(s) complied" + (nbUnknown > 0 ? " (" + nbUnknown + " unknown)" : ""));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (getCardinality()) {
            case -1 -> sb.append("all datasets ");
            case 0 -> sb.append("no dataset ");
            default -> sb.append(getCardinality()).append(" dataset(s) ");
        }
        sb.append("with the DICOM field ").append(getDicomTagCodeAndLabel(getDicomTag()))
            .append(" ").append(getOperation().name().toLowerCase())
            .append(" ")
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
