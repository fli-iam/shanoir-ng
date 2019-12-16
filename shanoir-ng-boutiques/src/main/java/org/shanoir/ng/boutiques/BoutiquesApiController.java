package org.shanoir.ng.boutiques;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;

import org.shanoir.ng.boutiques.model.BoutiquesTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.BufferedReader;

class BoutiquesProcess {
	Process process;
	BufferedReader inputBufferedReader;
	BufferedReader errorBufferedReader;
	public BoutiquesProcess(Process process) {
		this.process = process;
		this.inputBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}
}

@CrossOrigin(origins = "https://shanoir-ng-nginx")
@RestController
public class BoutiquesApiController implements BoutiquesApi {
	
	private static final int MAX_OUTPUT_LINES = 25;
	
	public static final Map<String, BoutiquesProcess> processes = new HashMap<String, BoutiquesProcess>();

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public BoutiquesApiController(HttpServletRequest request) {
        this.request = request;
    }
	
	@Autowired
	private SimpMessagingTemplate brokerMessagingTemplate;
    
	private void sendMessage(String message) throws Exception {
    	this.brokerMessagingTemplate.convertAndSend("/message/messages", message);
    }
    
    private void sendError(String message) throws Exception {
    	this.brokerMessagingTemplate.convertAndSend("/message/errors", message);
    }

	@Override
    public ArrayList<BoutiquesTool> searchTool(@RequestParam(value="query", defaultValue="") String query) {

        ArrayList<BoutiquesTool> searchResults = new ArrayList<BoutiquesTool>();
        try {
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " search " + query, null, output);
        	searchResults = BoutiquesUtils.parseBoutiquesSearch(output);
        	
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tool database not found");
        }

        return searchResults;
    }

	@Override
    public ArrayList<BoutiquesTool> getAllTools() {

    	Path filePath = Paths.get(System.getProperty("user.home"), ".cache", "boutiques", "descriptors.json");
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<BoutiquesTool> boutiquesTools = new ArrayList<BoutiquesTool>();
        try {
	        ObjectNode descriptors = objectMapper.readValue(filePath.toFile(), ObjectNode.class);

	        Iterator<Entry<String, JsonNode>> iter = descriptors.fields();
	        while (iter.hasNext()) {
	            Entry<String, JsonNode> entry = iter.next();
	            String toolId = entry.getKey();
	            JsonNode toolDescriptor = entry.getValue();
	            String name = toolDescriptor.get("name").asText();
	            String description = toolDescriptor.get("description").asText();
	            int nDownloads = toolDescriptor.get("nDownloads").asInt();
		        boutiquesTools.add(new BoutiquesTool(toolId, name, description, nDownloads));
	        }
	        
	        return boutiquesTools;
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tool database not found");
        }
    }

	@Override
    public ObjectNode getDescriptorById(@PathVariable String id) {

        String descriptorFileName = id.replace('.', '-') + ".json";
        ObjectMapper objectMapper = new ObjectMapper();

        try {
	        File file = Paths.get(System.getProperty("user.home") , ".cache", "boutiques", descriptorFileName).toFile();
	
	        ObjectNode descriptor = objectMapper.readValue(file, ObjectNode.class);

	        return descriptor;
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tool not found");
        }
    }

	@Override
    public String getInvocationById(@PathVariable String id, @RequestParam(value="complete", defaultValue="false") String completeString) {

        Boolean complete = Boolean.parseBoolean(completeString);

        try {
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " example " + (complete ? "--complete" : "") + " " + id, null, output);
        	
	        return String.join("\n", output);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while generating example invocation");
        }
    }

	@Override
    public String generateCommandById(@RequestBody ObjectNode invocation, @PathVariable String id) {

        try {
        	String invocationFilePath = BoutiquesUtils.writeTemporaryFile("invocation.json", invocation.toString());
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " exec simulate -i " + invocationFilePath + " " + id, null, output);
        	
	        return String.join("\n", output);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while generating tool command");
        }
    }

	@Override
    public String executeById(@RequestBody ObjectNode invocation, @PathVariable String toolId, @PathVariable String sessionId) {

        try {
        	// Get data from data path, use -v to mount the data path to docker container

//        	ArrayList<String> output = new ArrayList<String>();
//        	BoutiquesUtils.runCommandLineSync("docker volume inspect --format '{{.Mountpoint}}' shanoir-ng_tmp", null, output);
//        	String tmpVolume = output.get(0);

        	String tmpVolumeOnHost = System.getenv("BOUTIQUES_TMP_PATH_ON_HOST");;
        	
        	String inputPath = BoutiquesUtils.getInputPath();
        	
        	final String processId = BoutiquesUtils.getProcessId(toolId, sessionId);
        	String outputPath = BoutiquesUtils.getProcessOutputPath(processId);
//        	String outputPath = tmpVolumeOnHost + File.separator + "boutiques" + File.separator + "output";

        	BoutiquesUtils.createDirectory(outputPath);
			
			ObjectNode descriptor = getDescriptorById(toolId);

			BoutiquesUtils.setInputPaths(descriptor, invocation, inputPath);
			BoutiquesUtils.checkOutputPaths(descriptor, invocation);
		            	
        	String invocationFilePath = BoutiquesUtils.writeTemporaryFile("invocation.json", invocation.toString());
        	
        	String command = BoutiquesUtils.BOUTIQUES_COMMAND + " exec launch -s " + toolId + " " + invocationFilePath + " -v " + tmpVolumeOnHost + ":/tmp";
        	System.out.println(command);
        	BoutiquesProcess boutiquesProcess = processes.get(processId); 
        	if(boutiquesProcess != null) {
        		boutiquesProcess.process.destroy();
        	}
        	Process process = BoutiquesUtils.runCommandLineAsync(command, outputPath);
        	processes.put(processId, new BoutiquesProcess(process));
//        	BoutiquesUtils.sendProcessStreams(process, (message, isError)-> {
//        		try {
//            		if(isError) {
//            			this.sendError(message);
//            		} else {
//                		this.sendMessage(message);	
//            		}
//        		} catch (Exception ex) {
//        	        ex.printStackTrace();
//        	    }
//        	});

	        return "Execution started...";
        } catch (ResponseStatusException e) {
        	throw e;
        } catch (Exception e) {
            System.out.println("Error: " + e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while executing tool");
        }
    }

	@Override
    public ObjectNode getExecutionOutputById(@PathVariable String toolId, @PathVariable String sessionId) {

    	final String processId = BoutiquesUtils.getProcessId(toolId, sessionId);
    	
    	BoutiquesProcess boutiquesProcess = processes.get(processId);
    	if(boutiquesProcess != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode results = objectMapper.createObjectNode();
			
    		try {
    			ArrayNode inputLines = objectMapper.createArrayNode();
    			ArrayNode errorLines = objectMapper.createArrayNode();
    			boolean isAlive = boutiquesProcess.process.isAlive();
    			
    			String inputLine = null;
    			String errorLine = null;
    			while((isAlive && inputLines.size() == 0 || !isAlive && inputLines.size() < MAX_OUTPUT_LINES) && (inputLine = boutiquesProcess.inputBufferedReader.readLine()) != null) {
    				inputLines.add(inputLine);
    			}
    			while((isAlive && errorLines.size() == 0 || !isAlive && errorLines.size() < MAX_OUTPUT_LINES) && (errorLine = boutiquesProcess.errorBufferedReader.readLine()) != null) {
    				errorLines.add(errorLine);
    			}
    			
    			boolean finished = !isAlive && inputLine == null && errorLine == null;
    			if(finished) {
    				processes.remove(processId);
    			}
    			
    			results.set("input", inputLines);
    			results.set("error", errorLines);
    			results.put("finished", finished);
    	    } catch (IOException ex) {
                System.out.println("Server error: " + ex.getMessage());
    			results.put("error", "Server error while executing process.");
    	    }
			return results;
    	}
    	return null;
    }

	@Override
    public ResponseEntity<ByteArrayResource> downloadOutputById(@PathVariable String toolId, @PathVariable String sessionId) throws ResponseStatusException {

		try {
	    	final String processId = BoutiquesUtils.getProcessId(toolId, sessionId);
	    	File zipFile = new File(BoutiquesUtils.zipOutput(processId));

			byte[] data = Files.readAllBytes(zipFile.toPath());
			ByteArrayResource resource = new ByteArrayResource(data);
	
			// Try to determine file's content type
			String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

			return ResponseEntity.ok()
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
					.contentType(MediaType.parseMediaType(contentType))
					.contentLength(data.length)
					.body(resource);

		} catch (IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating zip file.");
		}
    }

	@Override
    public String updateDatabase() {
    	try {
			BoutiquesUtils.updateToolDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating tool database.");
		}
        return "Database update started.";
    }
}
