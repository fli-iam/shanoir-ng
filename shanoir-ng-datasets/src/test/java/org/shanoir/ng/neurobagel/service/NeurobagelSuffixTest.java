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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.shanoir.ng.neurobagel.service.NeurobagelSuffix.Modality;

/**
 * Natures: 1 T1, 2 T2, 3 T2*, 5 DWI, 9 spin tagging, 13/14 field maps, 18 FLAIR, 20 DSC perfusion.
 * Applications: 2 morphometry, 3 angiography, 7 perfusion, 8 diffusion, 9 BOLD.
 * An empty suffix means the dataset is left out of the imaging table.
 */
class NeurobagelSuffixTest {

    @ParameterizedTest(name = "{0} nature={1} application={2} -> {3}")
    @CsvSource(nullValues = "null", value = {
        // Non-MR modalities: always mapped
        "PET,   null, null, pet",
        "EEG,   null, null, eeg",
        "MEG,   null, null, meg",
        "OTHER, null, null, null",
        // Anatomy, from the nature
        "MR,    1,    2,    T1w",
        "MR,    2,    null, T2w",
        // BOLD, from the application, whatever the nature (Phase 0, study 46: EPI tagged T2 or T2*)
        "MR,    2,    9,    bold",
        "MR,    3,    9,    bold",
        "MR,    null, 9,    bold",
        // Field maps carrying their run's BOLD application are left out (study 46: B0 maps)
        "MR,    13,   9,    null",
        "MR,    14,   9,    null",
        // Diffusion, from the application or the nature
        "MR,    null, 8,    dwi",
        "MR,    5,    null, dwi",
        // ASL: spin tagging, or a perfusion application without a nature; not contrast perfusion (DSC)
        "MR,    9,    7,    asl",
        "MR,    null, 7,    asl",
        "MR,    20,   7,    null",
        // Not representable in Neurobagel, or not classified by the study cards
        "MR,    3,    null, null",
        "MR,    18,   null, null",
        "MR,    null, 3,    null",
        "MR,    null, null, null",
        // Unknown ids are treated as unclassified, not as errors
        "MR,    999,  999,  null",
    })
    void suffix(Modality modality, Integer nature, Integer application, String expected) {
        assertEquals(Optional.ofNullable(expected), NeurobagelSuffix.of(modality, nature, application));
    }

}
