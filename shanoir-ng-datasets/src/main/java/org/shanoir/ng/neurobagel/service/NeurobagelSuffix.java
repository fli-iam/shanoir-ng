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

package org.shanoir.ng.neurobagel.service;

import java.util.Optional;

import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequenceApplication;

/**
 * Maps a Shanoir dataset to the BIDS suffix Neurobagel expects in its imaging table.
 *
 * Neurobagel only knows T1w, T2w, dwi, bold, asl, eeg, meg and pet. The MR suffix comes from the
 * metadata set by the study cards (dataset nature and sequence application): anything they do not
 * classify, or classify as something Neurobagel cannot represent (T2*, FLAIR, angiography, field maps,
 * spectroscopy...), has no suffix and is left out. Never a guess.
 */
public final class NeurobagelSuffix {

    /** Kind of dataset, from its Shanoir dataset type. */
    public enum Modality { MR, PET, EEG, MEG, OTHER }

    private NeurobagelSuffix() { }

    public static Optional<String> of(Modality modality, Integer mrDatasetNature, Integer mrSequenceApplication) {
        if (modality == null) {
            return Optional.empty();
        }
        return switch (modality) {
            case PET -> Optional.of("pet");
            case EEG -> Optional.of("eeg");
            case MEG -> Optional.of("meg");
            case MR -> ofMr(nature(mrDatasetNature), application(mrSequenceApplication));
            default -> Optional.empty();
        };
    }

    private static Optional<String> ofMr(MrDatasetNature nature, MrSequenceApplication application) {
        // Field maps can carry the BOLD application of their run: check them first
        if (nature == MrDatasetNature.FIELD_MAP_DATASET_SHORT_ECHO_TIME
                || nature == MrDatasetNature.FIELD_MAP_DATASET_LONG_ECHO_TIME) {
            return Optional.empty();
        }
        if (application == MrSequenceApplication.BOLD) {
            return Optional.of("bold");
        }
        if (application == MrSequenceApplication.DIFFUSION || nature == MrDatasetNature.DIFFUSION_WEIGHTED_MR_DATASET) {
            return Optional.of("dwi");
        }
        // The PERFUSION application also covers contrast perfusion (DSC): only spin tagging is ASL
        if (nature == MrDatasetNature.SPIN_TAGGING_PERFUSION_MR_DATASET
                || (application == MrSequenceApplication.PERFUSION && nature == null)) {
            return Optional.of("asl");
        }
        if (nature == MrDatasetNature.T1_WEIGHTED_MR_DATASET) {
            return Optional.of("T1w");
        }
        if (nature == MrDatasetNature.T2_WEIGHTED_MR_DATASET) {
            return Optional.of("T2w");
        }
        return Optional.empty();
    }

    /** Unknown ids are treated as unclassified, not as errors: one bad value must not break the export. */
    private static MrDatasetNature nature(Integer id) {
        try {
            return MrDatasetNature.getNature(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static MrSequenceApplication application(Integer id) {
        try {
            return MrSequenceApplication.getApplication(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
