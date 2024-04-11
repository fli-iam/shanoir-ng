package org.shanoir.ng.vip.resulthandler;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.DatasetMetadataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OFSEPSeqIdHandler extends ResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OFSEPSeqIdHandler.class);

    public static final String PIPELINE_OUTPUT = "output.json";
    private static final String[] SERIE_PROPERTIES = {
            "coil",
            "type",
            "protocolValidityStatus",
            "name",
            "numberOfDirections",
            "deviceConstructor",
            "deviceMagneticField",
            "deviceModel",
            "deviceSerialNumber"
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

    public static final String TYPE = "type";
    public static final String SERIES = "series";
    public static final String ID = "id";
    public static final String VOLUMES = "volumes";
    public static final String ORIENTATION = "orientation";

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetAcquisitionService acquisitionService;

    @Autowired
    private WADODownloaderService wadoDownloaderService;

    @Autowired
    private DatasetPropertyService datasetPropertyService;

    @Autowired
    private SolrService solrService;


    @Override
    public boolean canProcess(ExecutionMonitoring processing) throws ResultHandlerException {
        if(processing.getPipelineIdentifier() == null || processing.getPipelineIdentifier().isEmpty()){
            throw new ResultHandlerException("Pipeline identifier is not set for processing [" + processing.getName() + "]", null);
        }
        return processing.getPipelineIdentifier().startsWith("ofsep_sequences_identification");
    }

    @Override
    public void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing) {

        for (File file : resultFiles) {
            if (!file.getName().equals(PIPELINE_OUTPUT)) {
                continue;
            }

            if (file.length() == 0) {
                LOG.error("File" + file.getName() + " is empty, this processing result won't be created.");
                continue;
            }

            try (InputStream is = new FileInputStream(file)) {
                JSONObject json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
                JSONArray series = json.getJSONArray(SERIES);

                if(series.length() < 1){
                    LOG.warn("Series list is empty in result file [{}].", file.getAbsolutePath());
                    return;
                }

                this.processSeries(series, processing);

            } catch (Exception e) {
                LOG.error("An error occured while extracting result from result archive.", e);
            }
        }
        LOG.error("Expected result file [" + parentFolder.getAbsolutePath() + "/" + PIPELINE_OUTPUT + "] is not present.");
    }

    /**
     * Process all series / acquisitions found in output JSON
     *
     * @param series JSONArray
     * @param execution ExecutionMonitoring
     * @throws JSONException
     * @throws PacsException
     * @throws EntityNotFoundException
     */
    public void processSeries(JSONArray series, ExecutionMonitoring execution) throws JSONException, PacsException, EntityNotFoundException, CheckedIllegalClassException, SolrServerException, IOException {

        for (int i = 0; i < series.length(); i++) {

            JSONObject serie = series.getJSONObject(i);
            Long serieId = serie.getLong(ID);


            List<Dataset> datasets = execution.getInputDatasets().stream()
                    .filter(ds -> ds.getDatasetAcquisition() != null
                            && ds.getDatasetAcquisition().getId().equals(serieId))
                    .collect(Collectors.toList());

            if(datasets.isEmpty()){
                LOG.error("No dataset found for serie/acquisition [" + serieId + "]");
                continue;
            }

            for(Dataset ds : datasets){

                Attributes attributes = this.getDicomAttributes(ds);

                JSONObject vol = this.getMatchingVolume(ds, serie, attributes);

                if(vol == null){
                    LOG.error("No volume from serie [{}] could be match with dataset [{}].", serieId, ds.getId());
                    continue;
                }

                try {
                    this.updateDataset(serie, ds, vol);
                } catch (CheckedIllegalClassException | EntityNotFoundException | SolrServerException | IOException e) {
                    LOG.error("Error while updating dataset [{}]", ds.getId(), e);
                    throw e;
                }

                List<DatasetProperty> properties = this.getDatasetPropertiesFromVolume(ds, vol, execution);
                properties.addAll(this.getDatasetPropertiesFromDicom(attributes, ds, execution));

                datasetPropertyService.createAll(properties);

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
    public void updateDataset(JSONObject serie, Dataset ds, JSONObject vol) throws JSONException, EntityNotFoundException, CheckedIllegalClassException, SolrServerException, IOException {

        DatasetMetadataField.NAME.update(ds, vol.getString(TYPE));
        datasetRepository.save(ds);

        if(ds.getDatasetAcquisition() instanceof MrDatasetAcquisition){
            DatasetAcquisition acq = ds.getDatasetAcquisition();
            DatasetAcquisitionMetadataField.MR_SEQUENCE_NAME.update(acq, serie.getString(TYPE));
            acquisitionService.update(acq);
        }

        solrService.updateDatasets(Arrays.asList(ds.getId()));

    }


    /**
     * Create dataset properties from pipeline output volume
     *
     * @param ds Dataset
     * @param volume JSONObject
     * @return
     */
    private List<DatasetProperty> getDatasetPropertiesFromVolume(Dataset ds, JSONObject volume, ExecutionMonitoring monitoring) throws JSONException {
        List<DatasetProperty> properties = new ArrayList<>();

        for(String name : SERIE_PROPERTIES){

            if(!volume.has(name)){
                continue;
            }

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("serie." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(monitoring);
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
            property.setProcessing(monitoring);
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
    public JSONObject getMatchingVolume(Dataset dataset, JSONObject serie, Attributes attributes) throws JSONException {

        if(serie.isNull(VOLUMES)){
            LOG.error("Volumes set is null in result file for serie [{}]", serie.getLong(ID));
            return null;
        }

        JSONArray volumes = serie.getJSONArray(VOLUMES);

        double[] dsOrientation = attributes.getDoubles(Tag.ImageOrientationPatient);

        for (int i = 0 ; i < volumes.length(); i++) {

            JSONObject volume = volumes.getJSONObject(i);

            if(volume.isNull(ORIENTATION)){
                LOG.error("Orientation is null in result file for volume [{}]", volume.getString(ID));
                continue;
            }

            JSONArray volOrientation = volume.getJSONArray(ORIENTATION);

            if(dsOrientation == null || dsOrientation.length == 0){
                LOG.error("ImageOrientationPatient DICOM property is empty for dataset [{}]", dataset.getId());
                continue;
            }

            if(volOrientation == null || volOrientation.length() == 0){
                LOG.error("Orientation is empty in result file for volume [{}]", volume.getString(ID));
                continue;
            }

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

        if(dsOrientation == null || dsOrientation.length == 0 || volOrientation == null || volOrientation.length() == 0){
            return false;
        }

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

    private List<DatasetProperty> getDatasetPropertiesFromDicom(Attributes attributes, Dataset ds, ExecutionMonitoring monitoring){

        List<DatasetProperty> properties = new ArrayList<>();

        DatasetProperty institutionName = new DatasetProperty();
        institutionName.setDataset(ds);
        institutionName.setName("dicom.InstitutionName");
        institutionName.setValue(attributes.getString(Tag.InstitutionName));
        institutionName.setProcessing(monitoring);
        properties.add(institutionName);

        DatasetProperty institutionAddress = new DatasetProperty();
        institutionAddress.setDataset(ds);
        institutionAddress.setName("dicom.InstitutionAddress");
        institutionAddress.setValue(attributes.getString(Tag.InstitutionAddress));
        institutionAddress.setProcessing(monitoring);
        properties.add(institutionAddress);

        return properties;
    }

    private Attributes getDicomAttributes(Dataset ds) throws PacsException {
        return wadoDownloaderService.getDicomAttributesForDataset(ds);
    }

}
