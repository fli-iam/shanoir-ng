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

package org.shanoir.ng.dataset.modality;

/**
 * This enumeration contains all 24 possible values for value 3
 * of image type (0008,0008) and frame type (0008,9007) tags/
 * attributes in dicom enhanced "Enhanced MR Image", so only for MR.
 * This value 3 is called image flavor. See more information according to:
 * https://www.dicomstandard.org/News-dir/ftsup/docs/sups/sup49.pdf
 * https://dicom.innolitics.com/ciods/enhanced-mr-image/enhanced-mr-image/00080008
 *
 * @author mkain
 *
 */
public enum ImageFlavor {

    ANGIO_TIME,

    METABOLITE_MAP,

    CINE,

    DIFFUSION,

    FLOW_ENCODED,

    FLUID_ATTENUATED,

    FMRI,

    LOCALIZER,

    MAX_IP,

    MIN_IP,

    M_MODE,

    MOTION,

    PERFUSION,

    PROTON_DENSITY,

    REALTIME,

    STIR,

    STRESS,

    TAGGING,

    TEMPERATURE,

    T1,

    T2,

    T2_STAR,

    TOF,

    VELOCITY

}
