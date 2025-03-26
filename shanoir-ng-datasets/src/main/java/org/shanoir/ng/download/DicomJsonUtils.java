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

package org.shanoir.ng.download;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to download files on using WADO URLs:
 *
 * WADO-RS URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.5.html
 * WADO-URI URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.2.html
 *
 * WADO-RS: http://dcm4chee-arc:8081/dcm4chee-arc/aets/DCM4CHEE/rs/studies/1.4.9.12.22.1.8447.5189520782175635475761938816300281982444
 * /series/1.4.9.12.22.1.3337.609981376830290333333439326036686033499
 * /instances/1.4.9.12.22.1.3327.13131999371192661094333587030092502791578
 *
 * As the responses are encoded as multipart/related messages,
 * this class extracts as well the files contained in the response to
 * the file system.
 *
 * WADO-URI: http://dcm4chee-arc:8081/dcm4chee-arc/aets/DCM4CHEE/wado?requestType=WADO
 * &studyUID=1.4.9.12.22.1.8444.518952078217568647576155668816300281982444
 * &seriesUID=1.4.9.12.22.1.8444.60998137683029030014444439326036686033499
 * &objectUID=1.4.9.12.22.1.8444.1313199937119266109555587030092502791578
 * &contentType=application/dicom
 *
 * WADO-URI Web Service Endpoint URL in dcm4chee arc light 5:
 * http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/wado
 *
 * This Spring service component uses the scope singleton, that is there by default,
 * as one instance should be reused for all other instances, that require usage.
 * No need to create multiple.
 *
 * @author mkain
 *
 */
public class DicomJsonUtils {

    public static final String STUDY_INSTANCE_UID = "0020000D";
    public static final String SERIE_INSTANCE_UID = "0020000E";
    public static final String OBJECT_INSTANCE_UID = "00080018";

    public static final String VALUE = "Value";

    public static final String STUDIES = "studies";
    public static final String SERIES = "series";
    public static final String INSTANCES = "instances";

    public static String inflateDCM4CheeJSON(String json) throws JSONException {
        JSONArray flat = new JSONArray(json);

        // 1st part : create the studies / seies / instances tree
        JSONArray studies = new JSONArray();
        for (int i = 0; i < flat.length(); i++) {
            JSONObject sop = flat.getJSONObject(i);

            JSONObject studyInstanceUIDObj = sop.getJSONObject(STUDY_INSTANCE_UID);
            JSONObject serieInstanceUIDObj = sop.getJSONObject(SERIE_INSTANCE_UID);

            String studyInstanceUID = studyInstanceUIDObj.getJSONArray(VALUE).getString(0);
            String serieInstanceUID = serieInstanceUIDObj.getJSONArray(VALUE).getString(0);

            // check studies has this study
            JSONObject studyObj = findStudy(studies, studyInstanceUID);
            if (studyObj == null) {
                // if not add it
                studyObj = new JSONObject(sop.toString()); // deep copy, will be used in part 2
                studyObj.put(SERIES, new JSONArray()); // put also an array to put SOPs
                studies.put(studyObj);
            }
            // check check series has this serie
            JSONArray series = studyObj.getJSONArray(SERIES);
            JSONObject serieObj = findSerie(series, serieInstanceUID);
            if (serieObj == null) {
                // if not add it
                serieObj = new JSONObject(sop.toString()); // deep copy of all the sop object, will be used in the 2nd part
                serieObj.put(INSTANCES, new JSONArray()); // put also an array to put SOPs
                series.put(serieObj);
            }
            // in any case, put the SOP into instances
            serieObj.getJSONArray(INSTANCES).put(sop);
        }

        // 2nd part : sort which property belong to which level
        // principle : if a property is the same on every object on a level, it belongs to the upper level
        for (int i = 0; i < studies.length(); i++) {
            JSONObject study = studies.getJSONObject(i);
            JSONArray series = study.getJSONArray(SERIES);
            for (int j = 0; j < series.length(); j++) {
                JSONObject serie = series.getJSONObject(j);
                JSONArray instances = serie.getJSONArray(INSTANCES);
                cleanParentWithOneChild(study, serie);
                for (int k = 0; k < instances.length(); k++) {
                    JSONObject instance = instances.getJSONObject(k);
                    cleanParentWithOneChild(serie, instance);
                }
            }
        }

        // 3rd part : remove useless children properties
        for (int i = 0; i < studies.length(); i++) {
            JSONObject study = studies.getJSONObject(i);
            JSONArray series = study.getJSONArray(SERIES);
            for (int j = 0; j < series.length(); j++) {
                JSONObject serie = series.getJSONObject(j);
                JSONArray instances = serie.getJSONArray(INSTANCES);
                for (int k = 0; k < instances.length(); k++) {
                    JSONObject instance = instances.getJSONObject(k);
                    cleanChildWithParent(serie, instance, OBJECT_INSTANCE_UID);
                }
                cleanChildWithParent(study, serie, SERIE_INSTANCE_UID);
            }
        }

        JSONObject tree = new JSONObject();
        tree.put(STUDIES, studies);
        return tree.toString();
    }

    private static void cleanParentWithOneChild(JSONObject parent, JSONObject child) throws JSONException {
        JSONArray properties = child.names();
        for (int l = 0; l < properties.length(); l++) {
            String propertyName = properties.getString(l);
            Object property = child.get(propertyName);
            if (property instanceof JSONObject) {
                if (parent.has(propertyName) && !JSONUtils.equals((JSONObject)property, parent.getJSONObject(propertyName))) {
                    parent.remove(propertyName);
                }
            }
        }
    }

    private static void cleanChildWithParent(JSONObject parent, JSONObject child, String except) throws JSONException {
        JSONArray properties = child.names();
        for (int l = 0; l < properties.length(); l++) {
            String propertyName = properties.getString(l);
            if (!except.equals(propertyName) && parent.has(propertyName)) {
                child.remove(propertyName);
            }
        }
    }

    private static JSONObject findStudy(JSONArray studies, String instanceUID) throws JSONException {
        return findObjectInArrByTagFirstValue(studies, STUDY_INSTANCE_UID, instanceUID);
    }

    private static JSONObject findSerie(JSONArray series, String instanceUID) throws JSONException {
        return findObjectInArrByTagFirstValue(series, SERIE_INSTANCE_UID, instanceUID);
    }

    private static JSONObject findObjectInArrByTagFirstValue(JSONArray arr, String tag, String value) throws JSONException {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (extractFirstValueForTag(obj, tag).equals(value)) {
                return obj;
            }
        }
        return null;
    }

    private static String extractFirstValueForTag(JSONObject obj, String tag) throws JSONException {
        JSONObject subObj = obj.getJSONObject(tag);
        JSONArray values = subObj.getJSONArray(VALUE);
        if (values.length() == 0) throw new JSONException("Could not find values in  tag " + tag + " for obj : " + subObj.toString());
        return values.getString(0);
    }

}