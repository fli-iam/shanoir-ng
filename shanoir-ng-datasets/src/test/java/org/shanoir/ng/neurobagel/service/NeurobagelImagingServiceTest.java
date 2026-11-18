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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRepository;
import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRow;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NeurobagelImagingServiceTest {

    private static final String HEADER = "sub\tses\tsuffix\tpath\n";

    @Mock
    private NeurobagelImagingRepository imagingRepository;

    @InjectMocks
    private NeurobagelImagingService service;

    @TempDir
    private File bidsDataDir;

    @Test
    void imagingTableKeepsOnlyTheDatasetsWithASuffix() {
        given(imagingRepository.findImagingRows(1L)).willReturn(List.of(
                row(10L, 100L, 1000L, "MR", 1, 2),       // T1w
                row(10L, 100L, 1001L, "MR", 2, 9),       // bold
                row(10L, 100L, 1002L, "MR", 13, 9),      // field map: left out
                row(11L, 101L, 1003L, "MR", null, null), // not classified: left out
                row(11L, 101L, 1004L, "PET", null, null),
                row(11L, 101L, 1005L, "OTHER", null, null))); // CT: left out

        assertEquals(HEADER
                + "sub-10\tses-100\tT1w\t/shanoir/examination/100/dataset/1000\n"
                + "sub-10\tses-100\tbold\t/shanoir/examination/100/dataset/1001\n"
                + "sub-11\tses-101\tpet\t/shanoir/examination/101/dataset/1004\n", service.buildImagingTsv(1L));
    }

    @Test
    void imagingTableWithoutAnySuffixHasTheHeaderOnly() {
        given(imagingRepository.findImagingRows(1L)).willReturn(List.of(row(10L, 100L, 1000L, "MR", null, null)));

        assertEquals(HEADER, service.buildImagingTsv(1L));
    }

    @Test
    void imagingTableIsWrittenIntoTheSharedFolderUnderItsReturnedName() throws Exception {
        ReflectionTestUtils.setField(service, "bidsDataDir", bidsDataDir.getAbsolutePath());
        given(imagingRepository.findImagingRows(7L)).willReturn(List.of(row(10L, 100L, 1000L, "PET", null, null)));

        String fileName = service.writeImagingTsv(7L);

        assertTrue(fileName.matches("study-7-imaging-[0-9a-f-]{36}\\.tsv"), fileName);
        Path file = bidsDataDir.toPath().resolve(NeurobagelImagingService.FOLDER).resolve(fileName);
        assertEquals(HEADER + "sub-10\tses-100\tpet\t/shanoir/examination/100/dataset/1000\n",
                Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void failureToWriteRepliesNull() throws Exception {
        // the shared folder path is a regular file: the neurobagel folder cannot be created
        File notAFolder = new File(bidsDataDir, "not-a-folder");
        Files.writeString(notAFolder.toPath(), "x");
        ReflectionTestUtils.setField(service, "bidsDataDir", notAFolder.getAbsolutePath());

        assertNull(service.writeImagingTsv(7L));
    }

    private static NeurobagelImagingRow row(Long subjectId, Long examinationId, Long datasetId, String modality,
            Integer nature, Integer application) {
        return new NeurobagelImagingRow() {
            public Long getSubjectId() {
                return subjectId;
            }

            public Long getExaminationId() {
                return examinationId;
            }

            public Long getDatasetId() {
                return datasetId;
            }

            public String getModality() {
                return modality;
            }

            public Integer getMrDatasetNature() {
                return nature;
            }

            public Integer getMrSequenceApplication() {
                return application;
            }
        };
    }

}
