package org.shanoir.ng.processing.carmin.output;

import org.apache.commons.io.IOUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.service.ProcessingResourceService;
import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.DatasetMetadataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OFSEPSeqIdProcessing extends OutputProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(OFSEPSeqIdProcessing.class);

    public static final String PIPELINE_OUTPUT = "output.json";

    private static final String[] SERIE_PROPERTIES = {
            "coil",
            "type",
            "protocolValidityStatus",
            "name",
            "numberOfDirections"
    };

    private static final String[] VOLUME_PROPERTIES = {
            "acquisitionDate",
            "contrastAgent",
            "contrastAgentAlgo",
            "contrastAgentAlgoConfidence",
            "contrastAgentDICOM",
            "organ",
            "organAlgo",
            "organAlgoConfidence",
            "organDICOM",
            "type",
            "sequence",
            "extraType",
            "derivedSequence",
            "name",
            "contrast",
            "bValue",
            "sliceThickness",
            "spacings",
            "spacingBetweenSlices",
            "numberOfSlices",
            "dimension",
            "dimensions",
            "axis"

    };

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    @Autowired
    private WADODownloaderService wadoDownloaderService;

    @Autowired
    private DatasetPropertyService datasetPropertyService;

    @Autowired
    private ProcessingResourceService processingResourceService;



    @Override
    public boolean canProcess(CarminDatasetProcessing processing) {
        return processing.getPipelineIdentifier().startsWith("ofsep_sequences_identification");
    }

    @Override
    public void manageTarGzResult(List<File> resultFiles, File parentFolder, CarminDatasetProcessing processing) throws OutputProcessingException {

        for(File file : resultFiles){
            if (file.getAbsolutePath().endsWith(PIPELINE_OUTPUT)) {

                if (file.length() == 0) {
                    LOG.warn("Result file [{}] is present but empty.", file.getAbsolutePath());
                    return;
                }

                try (InputStream is = new FileInputStream(file)) {
                    JSONObject json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
                    this.processSeries(json.getJSONArray("series"), processing);
                }catch (Exception e) {
                    throw new OutputProcessingException("An error occured while extracting result from result archive.", e);
                }

                return;
            }
        }

    }

    /**
     * Process all series / acquisitions found in output JSON
     *
     * @param series JSONArray
     * @param processing CarminDatasetProcessing
     * @throws JSONException
     * @throws PacsException
     * @throws EntityNotFoundException
     */
    public void processSeries(JSONArray series, CarminDatasetProcessing processing) throws JSONException, PacsException, EntityNotFoundException, CheckedIllegalClassException {

        for (int i = 0; i < series.length(); i++) {

            JSONObject serie = series.getJSONObject(i);
            Long serieId = serie.getLong("id");


            List<Dataset> datasets = processing.getInputDatasets().stream()
                    .filter(ds -> ds.getDatasetAcquisition() != null
                            && ds.getDatasetAcquisition().getId().equals(serieId))
                    .collect(Collectors.toList());

            if(datasets.isEmpty()){
                LOG.error("No dataset found for serie/acquisition [" + serieId + "]");
                continue;
            }

            for(Dataset ds : datasets){

                JSONObject vol;
                vol = this.getMatchingVolume(ds, serie);

                if(vol == null){
                    LOG.error("No volume from serie [{}] could be match with dataset [{}].", serieId, ds.getId());
                    continue;
                }

                try {
                    this.updateDataset(serie, ds, vol);
                } catch (CheckedIllegalClassException | EntityNotFoundException e) {
                    LOG.error("Error while updating dataset [{}]", ds.getId(), e);
                    throw e;
                }

                List<DatasetProperty> properties = this.getDatasetPropertiesFromVolume(ds, vol, processing);
                datasetPropertyService.create(properties);

            }
        }
    }

    /**
     * Update dataset from pipeline output serie & volume
     *
     * @param serie JSONObject
     * @param ds Dataset
     * @param vol JSONObject
     * @throws JSONException
     * @throws EntityNotFoundException
     */
    public void updateDataset(JSONObject serie, Dataset ds, JSONObject vol) throws JSONException, EntityNotFoundException, CheckedIllegalClassException {

        DatasetMetadataField.NAME.update(ds, vol.getString("type"));
        datasetRepository.save(ds);

        if(ds.getDatasetAcquisition() instanceof MrDatasetAcquisition){
            DatasetAcquisition acq = ds.getDatasetAcquisition();
            DatasetAcquisitionMetadataField.MR_SEQUENCE_NAME.update(acq, serie.getString("type"));
            acquisitionService.update(acq);
        }

    }


    /**
     * Create dataset properties from pipeline output volume
     *
     * @param ds Dataset
     * @param volume JSONObject
     * @return
     */
    private List<DatasetProperty> getDatasetPropertiesFromVolume(Dataset ds, JSONObject volume, CarminDatasetProcessing processing) throws JSONException {
        List<DatasetProperty> properties = new ArrayList<>();

        for(String name : SERIE_PROPERTIES){

            if(!volume.has(name)){
                continue;
            }

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("serie." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        for(String name : VOLUME_PROPERTIES){

            if(!volume.has(name)){
                continue;
            }

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("volume." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        return properties;

    }


    /**
     * Return JSON volume matching Shanoir dataset
     * Match is made by orientation DICOM property
     *
     * @param dataset Dataset
     * @param serie JSONObject
     *
     * @return JSONObject
     * @throws JSONException
     */
    public JSONObject getMatchingVolume(Dataset dataset, JSONObject serie) throws JSONException, PacsException {

        JSONArray volumes = serie.getJSONArray("volumes");

        Attributes attributes = wadoDownloaderService.getDicomAttributesForDataset(dataset);

        double[] dsOrientation = attributes.getDoubles(Tag.ImageOrientationPatient);

        for (int i = 0 ; i < volumes.length(); i++) {

            JSONObject volume = volumes.getJSONObject(i);
            JSONArray volOrientation = volume.getJSONArray("orientation");

            if(this.areOrientationsEquals(dsOrientation, volOrientation)){
                return volume;
            }

        }

        return null;

    }

    /**
     * Check if the two arrays are equals
     *
     * @param dsOrientation double[]
     * @param volOrientation JSONArray
     * @return boolean
     * @throws JSONException
     */
    public boolean areOrientationsEquals(double[] dsOrientation, JSONArray volOrientation) throws JSONException {

        if(dsOrientation.length != volOrientation.length()){
            return false;
        }

        for (int i = 0 ; i < dsOrientation.length; i++) {
            if(dsOrientation[i] != volOrientation.getDouble(i)){
                return false;
            }
        }

        return true;

    }

}
