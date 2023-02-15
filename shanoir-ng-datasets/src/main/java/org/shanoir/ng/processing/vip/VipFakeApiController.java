package org.shanoir.ng.processing.vip;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class VipFakeApiController implements VipFakeApi {

	private static final Logger LOG = LoggerFactory.getLogger(VipFakeApiController.class);

	@Autowired
	ObjectMapper mapper;
	
	public ResponseEntity<List<Pipeline>> getProcessing() throws JsonMappingException, JsonProcessingException {
		String content = 
		"{\n"
		+ "	'identifier':'ct-tiqua/1.4',\n"
		+ "	'name':'ct-tiqua',\n"
		+ "	'description':'Computed Tomography based Traumatic Brain Injury Quantification. This software takes as input a CT-scan of a head injured patient and returns: 1/ the segmentation of 7 types of lesions typical from TBI, 2/ a structural atlas dividing the input brain in 10 zones, and 3/ a csv file containing the volume in mm3 of all type on lesion in all zone of the brain. It was developed by Clement Brossard and Benjamin Lemasson (benjamin.lemasson@univ-grenoble-alpes.fr). We are currently writing a scientific article descibing the whole process. If you use our software, please cite our work!',\n"
		+ "	'version':'1.4',\n"
		+ "	'parameters':\n"
		+ "	[{\n"
		+ "		'name':'ensemble',\n"
		+ "		'type':'Boolean',\n"
		+ "		'description':'For the segmentation step: Whether to use all the 12 CNN models rather than only one (slower but more precise).',\n"
		+ "		'isOptional':true,\n"
		+ "		'isReturnedValue':false\n"
		+ "	},{\n"
		+ "		'name':'keep_tmp_files',\n"
		+ "		'type':'Boolean',\n"
		+ "		'description':'Do not remove temporary files at the end of the pipeline, ie. include them in the output archive.',\n"
		+ "		'isOptional':true,\n"
		+ "		'isReturnedValue':false\n"
		+ "	},{\n"
		+ "		'name':'infile',\n"
		+ "		'type':'File',\n"
		+ "		'description':'Input image (e.g. img.nii, img.nii.gz) to be processed. It must be a brain CT-scan.',\n"
		+ "		'isOptional':false,\n"
		+ "		'isReturnedValue':false\n"
		+ "	}],\n"
		+ "	'canExecute':true\n"
		+ "}\n";
		Pipeline pipeline = mapper.readValue(content, Pipeline.class);
		return new ResponseEntity<List<Pipeline>>(Collections.singletonList(pipeline), HttpStatus.OK);
	}

	public void launchProcessing() {

	}

}
