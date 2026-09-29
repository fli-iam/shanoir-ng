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

package org.shanoir.ng.dicom.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.examination.service.ExaminationService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests of the replacement of the real StudyInstanceUID with the
 * examinationUID in a DICOMWeb metadata response, in particular for study
 * references carried inside nested sequences, as an RT Structure Set does.
 */
@ExtendWith(MockitoExtension.class)
class StudyInstanceUIDAndSubjectNameHandlerTest {

    private static final String STUDY_UID = "1.4.9.12.34.1.8527.1111111111111111111111111111111111111111";

    private static final String FOREIGN_STUDY_UID = "1.4.9.12.34.1.8527.9999999999999999999999999999999999999999";

    private static final String SOP_INSTANCE_UID = "1.4.9.12.34.1.8527.2222222222222222222222222222222222222222";

    private static final String EXAMINATION_UID = "1.4.9.12.34.1.8527.42";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private ExaminationService examinationService;

    @InjectMocks
    private StudyInstanceUIDAndSubjectNameHandler handler;

    @Test
    void replaceStudyInstanceUIDReplacesTopLevelStudyInstanceUIDAndRetrieveURL() throws JsonProcessingException {
        JsonNode root = MAPPER.readTree("""
                {
                  "0020000D": { "vr": "UI", "Value": ["%s"] },
                  "00081190": { "vr": "UR", "Value": ["http://pacs/rs/studies/%s"] }
                }
                """.formatted(STUDY_UID, STUDY_UID));

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        assertEquals(EXAMINATION_UID, root.at("/0020000D/Value/0").asText());
        assertEquals("http://pacs/rs/studies/" + EXAMINATION_UID, root.at("/00081190/Value/0").asText());
    }

    @Test
    void replaceStudyInstanceUIDReplacesReferenceInReferencedStudySequence() throws JsonProcessingException {
        // 0008,1110 ReferencedStudySequence -> 0008,1155 ReferencedSOPInstanceUID
        JsonNode root = MAPPER.readTree("""
                {
                  "00081110": { "vr": "SQ", "Value": [
                    {
                      "00081150": { "vr": "UI", "Value": ["1.2.840.10008.3.1.2.3.1"] },
                      "00081155": { "vr": "UI", "Value": ["%s"] }
                    }
                  ]}
                }
                """.formatted(STUDY_UID));

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        assertEquals(EXAMINATION_UID, root.at("/00081110/Value/0/00081155/Value/0").asText());
    }

    @Test
    void replaceStudyInstanceUIDReplacesReferenceNestedInRTReferencedStudySequence() throws JsonProcessingException {
        JsonNode root = MAPPER.readTree(rtStructMetadata());

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        // 3006,0010 -> 3006,0012 -> 0008,1155 carries the StudyInstanceUID
        assertEquals(EXAMINATION_UID,
                root.at("/30060010/Value/0/30060012/Value/0/00081155/Value/0").asText());
    }

    @Test
    void replaceStudyInstanceUIDLeavesSOPInstanceUIDOfContourImageSequenceUntouched() throws JsonProcessingException {
        JsonNode root = MAPPER.readTree(rtStructMetadata());

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        // 3006,0016 ContourImageSequence -> 0008,1155 carries a SOP Instance UID,
        // that never equals the StudyInstanceUID: it is not virtualised
        assertEquals(SOP_INSTANCE_UID, root.at(
                "/30060010/Value/0/30060012/Value/0/30060014/Value/0/30060016/Value/0/00081155/Value/0").asText());
    }

    @Test
    void replaceStudyInstanceUIDLeavesForeignStudyInstanceUIDUntouched() throws JsonProcessingException {
        // a reference to another study, e.g. a prior, is not this examination:
        // rewriting it would assert a false identity
        JsonNode root = MAPPER.readTree("""
                {
                  "00081110": { "vr": "SQ", "Value": [
                    { "00081155": { "vr": "UI", "Value": ["%s"] } }
                  ]}
                }
                """.formatted(FOREIGN_STUDY_UID));

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        assertEquals(FOREIGN_STUDY_UID, root.at("/00081110/Value/0/00081155/Value/0").asText());
    }

    @Test
    void replaceStudyInstanceUIDWalksTheTopLevelArrayOfMetadataObjects() throws JsonProcessingException {
        JsonNode root = MAPPER.readTree("""
                [
                  { "0020000D": { "vr": "UI", "Value": ["%s"] } },
                  { "0020000D": { "vr": "UI", "Value": ["%s"] } }
                ]
                """.formatted(STUDY_UID, STUDY_UID));

        handler.replaceStudyInstanceUID(root, STUDY_UID, EXAMINATION_UID);

        assertEquals(EXAMINATION_UID, root.at("/0/0020000D/Value/0").asText());
        assertEquals(EXAMINATION_UID, root.at("/1/0020000D/Value/0").asText());
    }

    /**
     * An RT Structure Set references its source study through
     * ReferencedFrameOfReferenceSequence (3006,0010) ->
     * RTReferencedStudySequence (3006,0012), where the ReferencedSOPInstanceUID
     * (0008,1155) carries the StudyInstanceUID, and its images through
     * RTReferencedSeriesSequence (3006,0014) -> ContourImageSequence (3006,0016),
     * where the same tag carries SOP Instance UIDs.
     */
    private String rtStructMetadata() {
        return """
                {
                  "0020000D": { "vr": "UI", "Value": ["%s"] },
                  "30060010": { "vr": "SQ", "Value": [
                    {
                      "30060012": { "vr": "SQ", "Value": [
                        {
                          "00081150": { "vr": "UI", "Value": ["1.2.840.10008.3.1.2.3.1"] },
                          "00081155": { "vr": "UI", "Value": ["%s"] },
                          "30060014": { "vr": "SQ", "Value": [
                            {
                              "0020000E": { "vr": "UI", "Value": ["1.4.9.12.34.1.8527.3333"] },
                              "30060016": { "vr": "SQ", "Value": [
                                {
                                  "00081150": { "vr": "UI", "Value": ["1.2.840.10008.5.1.4.1.1.4"] },
                                  "00081155": { "vr": "UI", "Value": ["%s"] }
                                }
                              ]}
                            }
                          ]}
                        }
                      ]}
                    }
                  ]}
                }
                """.formatted(STUDY_UID, STUDY_UID, SOP_INSTANCE_UID);
    }

}
