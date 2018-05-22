package org.shanoir.ng.preclinical.extra_data.bloodgas_data;

import javax.persistence.Entity;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
/**
 * Blood gas Data
 */
@Entity
@JsonPropertyOrder({ "_links", "examinationId", "filename" })
public class BloodGasData extends ExaminationExtraData   {


}

