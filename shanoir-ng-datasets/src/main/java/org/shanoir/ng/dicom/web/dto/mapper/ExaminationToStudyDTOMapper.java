package org.shanoir.ng.dicom.web.dto.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.dicom.web.dto.InstanceDTO;
import org.shanoir.ng.dicom.web.dto.MetadataDTO;
import org.shanoir.ng.dicom.web.dto.SerieDTO;
import org.shanoir.ng.dicom.web.dto.StudyDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class maps the Examination objects from the Shanoir-NG database
 * to a DICOM-study DTO to implement the DICOMweb protocol.
 *
 * @author mkain
 *
 */
@Mapper(componentModel = "spring")
public abstract class ExaminationToStudyDTOMapper {

    public StudyDTO examinationToStudyDTO(Examination examination) {
        StudyDTO studyDTO = new StudyDTO();
        final String studyInstanceUID = UIDGeneration.ROOT + ".1." + examination.getId();
        // 5 DICOM-study specific values
        studyDTO.setStudyInstanceUID(studyInstanceUID);
        studyDTO.setStudyID(examination.getId());
        studyDTO.setStudyDescription(examination.getComment());
//        studyDTO.setStudyDate(examination.getExaminationDate().toString());
        studyDTO.setStudyDate("20220303");
        studyDTO.setStudyTime("000000"); // today we do not store this info in our db
        studyDTO.setAccessionNumber("");
        // 4 patient specific values
        // @TODO optimize here: not ask the database for each subject id, use a cached list?
        String subjectName = examination.getSubject() != null ? examination.getSubject().getName() : null;
        studyDTO.setPatientName(subjectName);
        studyDTO.setPatientID(subjectName);
        studyDTO.setPatientBirthDate("01011960"); // @TODO not yet in ms datasets database
        studyDTO.setPatientSex("F"); // @TODO not yet in ms datasets database

        addSeries(examination, studyDTO);
        studyDTO.setNumInstances(1);
        if (studyDTO.getSeries().size() > 0) {
            studyDTO.setModalities(studyDTO.getSeries().get(0).getModality());
        }

        return studyDTO;
    }

    /**
     * This method transforms dataset acquisitions in Shanoir back
     * into DICOM series for the purpose of supporting the DICOMWeb protocol.
     *
     * @param examination
     * @param studyDTO
     */
    private void addSeries(Examination examination, StudyDTO studyDTO) {
        List<SerieDTO> series = new ArrayList<SerieDTO>();
        List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
        for (DatasetAcquisition datasetAcquisition : acquisitions) {
            SerieDTO serie = new SerieDTO();
            final String serieInstanceUID = UIDGeneration.ROOT + ".2." + datasetAcquisition.getId();
            serie.setSerieInstanceUID(serieInstanceUID);
            serie.setSeriesNumber(datasetAcquisition.getSortingIndex());
            serie.setModality(datasetAcquisition.getType().toUpperCase());

            List<InstanceDTO> instances = new ArrayList<InstanceDTO>();
            List<Dataset> datasets = datasetAcquisition.getDatasets();
            for (Dataset dataset : datasets) {
                InstanceDTO instance = new InstanceDTO();
                MetadataDTO metadata = new MetadataDTO();
                metadata.setSopClassUID("1.2.840.10008.5.1.4.1.1.2");
                metadata.setModality("MR");
                metadata.setInstanceNumber(1);
                metadata.setStudyInstanceUID(studyDTO.getStudyInstanceUID());
                metadata.setSeriesInstanceUID(serie.getSerieInstanceUID());
                metadata.setSopInstanceUID(UIDGeneration.ROOT + ".3." + dataset.getId());
                metadata.setColumns(512);
                metadata.setRows(512);
                metadata.setSeriesDate(studyDTO.getStudyDate());
                instance.setMetadata(metadata);
                instance.setUrl("dicomweb://https://shanoir-ng-nginx/fakeURL.dcm");
                instances.add(instance);
            }
            serie.setInstances(instances);
            series.add(serie);
        }
        studyDTO.setSeries(series);
    }

    /**
     * Map list of @Examination to list of @StudyDTO.
     *
     * @param examinations
     * @return list of StudyDTO
     */
    public abstract List<StudyDTO> examinationsToStudyDTOs(List<Examination> examinations);

}
