package org.shanoir.ng.boutiques;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;

import org.shanoir.ng.boutiques.model.BoutiquesTool;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
//import org.shanoir.ng.dataset.model.Dataset;
//import org.shanoir.ng.dataset.model.DatasetExpression;
//import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
//import org.shanoir.ng.dataset.service.DatasetService;
//import org.shanoir.ng.datasetfile.DatasetFile;
//import org.shanoir.ng.download.WADODownloaderService;
//import org.shanoir.ng.shared.exception.ErrorModel;
//import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//import io.swagger.annotations.ApiParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.net.MalformedURLException;
//import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

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

//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "https://shanoir-ng-nginx")
@RestController
public class BoutiquesController {

	private static final String ZIP = ".zip";

	private static final String DOWNLOAD = ".download";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final SecureRandom RANDOM = new SecureRandom();
	
	public static final Map<String, BoutiquesProcess> processes = new HashMap<String, BoutiquesProcess>();

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public BoutiquesController(HttpServletRequest request) {
        this.request = request;
    }
    
//	@Autowired
//	private DatasetService datasetService;
//	
//	@Autowired
//	private WADODownloaderService downloader;
	
	@Autowired
	private SimpMessagingTemplate brokerMessagingTemplate;
    
	private void sendMessage(String message) throws Exception {
    	this.brokerMessagingTemplate.convertAndSend("/message/messages", message);
    }
    
    private void sendError(String message) throws Exception {
    	this.brokerMessagingTemplate.convertAndSend("/message/errors", message);
    }

//    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    @GetMapping("/tool/search")
    public ArrayList<BoutiquesTool> searchTool(@RequestParam(value="query", defaultValue="") String query) {

        ArrayList<BoutiquesTool> searchResults = new ArrayList<BoutiquesTool>();
        try {
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " search " + query, null, output);
        	searchResults = BoutiquesUtils.parseBoutiquesSearch(output);
        	
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return searchResults;
    }

    @GetMapping("/tool/all")
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
	            String name = toolDescriptor.findValue("name").asText();
	            String description = toolDescriptor.findValue("description").asText();
	            int nDownloads = toolDescriptor.findValue("nDownloads").asInt();
		        boutiquesTools.add(new BoutiquesTool(toolId, name, description, nDownloads));
	        }
	        
	        return boutiquesTools;
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }
    }
    
    @GetMapping("/tool/{id}/descriptor/")
    public ObjectNode getDescriptorById(@PathVariable String id) {

        String descriptorFileName = id.replace('.', '-') + ".json";
        ObjectMapper objectMapper = new ObjectMapper();

        try {
	        File file = Paths.get(System.getProperty("user.home") , ".cache", "boutiques", descriptorFileName).toFile();
	
	        ObjectNode descriptor = objectMapper.readValue(file, ObjectNode.class);

	        return descriptor;
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }
    }
    
    @GetMapping("/tool/{id}/invocation")
    public String getInvocationById(@PathVariable String id, @RequestParam(value="complete", defaultValue="false") String completeString) {

        Boolean complete = Boolean.parseBoolean(completeString);

        try {
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " example " + (complete ? "--complete" : "") + " " + id, null, output);
        	
	        return String.join("\n", output);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }
    }
    
    @PostMapping("/tool/{id}/generate-command/")
    public String generateCommandById(@RequestBody ObjectNode invocation, @PathVariable String id) {

        try {
        	String invocationFilePath = BoutiquesUtils.writeTemporaryFile("invocation.json", invocation.toString());
        	ArrayList<String> output = new ArrayList<String>();
        	BoutiquesUtils.runCommandLineSync(BoutiquesUtils.BOUTIQUES_COMMAND + " exec simulate -i " + invocationFilePath + " " + id, null, output);
        	
	        return String.join("\n", output);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }
    }

    private String getProcessId(String id) {
		final Long userId = KeycloakUtil.getTokenUserId();
		return id + Long.toString(userId);
    }

    private String getOutputPath(String processId) {
    	String outputPath = System.getenv("BOUTIQUES_OUTPUT_PATH");
    	if(outputPath == null) {
    		outputPath = "/output";
    	}
    	outputPath += "/" + processId;
    	return outputPath;
    }
    
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tool/{id}/execute/")
    public String executeById(@RequestBody ObjectNode invocation, @PathVariable String id) {

        try {
        	// Get data from data path, use -v to mount the data path to docker container

        	String dataPath = System.getenv("BOUTIQUES_DATA_PATH");
        	if(dataPath == null) {
        		dataPath = "/tmp"; 		// "/var/lib/docker/volumes/shanoir-ng_tmp/_data";
        	}
        	final String processId = getProcessId(id);
        	String outputPath = getOutputPath(processId);

			final File outputDir = new File(outputPath);
			if (!outputDir.exists()) {
				outputDir.mkdirs(); // create if not yet existing
			}
			
//        	ObjectNode descriptor = getDescriptorById(id);
//        	for() {
//        		
//        	}
        	
        	String invocationFilePath = BoutiquesUtils.writeTemporaryFile("invocation.json", invocation.toString());
        	
        	String command = BoutiquesUtils.BOUTIQUES_COMMAND + " exec launch -s " + id + " " + invocationFilePath + " -v " + dataPath + ":/tmp/";
        	System.out.println(command);
        	BoutiquesProcess boutiquesProcess = processes.get(processId); 
        	if(boutiquesProcess != null) {
        		boutiquesProcess.process.destroy();
        	}
        	Process process = BoutiquesUtils.runCommandLineAsync(command, outputPath);
        	processes.put(processId, new BoutiquesProcess(process));
        	BoutiquesUtils.sendProcessStreams(process, (message, isError)-> {
        		try {
            		if(isError) {
            			this.sendError(message);
            		} else {
                		this.sendMessage(message);	
            		}
        		} catch (Exception ex) {
        	        ex.printStackTrace();
        	    }
        	});

	        return "Execution started...";
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return "Error: " + e;
        }
    }

	/**
	 * Zip
	 * 
	 * @param sourceDirPath
	 * @param zipFilePath
	 * @throws IOException
	 */
	private void zip(String sourceDirPath, String zipFilePath) throws IOException {
		Path p = Paths.get(zipFilePath);
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp)
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
//						LOG.error(e.getMessage(), e);
					}
				});
            	zos.finish();
            zos.close();
		}
	}
	
    private String zipOutput(String processId) throws IOException {
    	String outputPath = getOutputPath(processId);
    	zip(outputPath, outputPath + ".zip");
    	return outputPath + ".zip";
    }
    
    @PostMapping("/tool/{id}/output/")
    public ObjectNode getExecutionOutputById(@PathVariable String id) {

    	final String processId = getProcessId(id);
    	
    	BoutiquesProcess boutiquesProcess = processes.get(processId);
    	if(boutiquesProcess != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode results = objectMapper.createObjectNode();
			
    		try {
    			String inputLine = boutiquesProcess.inputBufferedReader.readLine();
    			String errorLine = boutiquesProcess.errorBufferedReader.readLine();
    			results.put("input", inputLine);
    			results.put("error", errorLine);
    			if(inputLine == null && errorLine == null && !boutiquesProcess.process.isAlive()) {
        			results.put("finished", true);
    			}
    	    } catch (IOException ex) {
                System.out.println("Server error: " + ex.getMessage());
    			results.put("server error", "Server error while executing process.");
    	    }
			return results;
    	}
    	return null;
    }

    @PostMapping("/tool/{id}/download-output/")
    public ResponseEntity<ByteArrayResource> downloadOutputById(@PathVariable String id) throws RestServiceException {

		try {
	    	final String processId = getProcessId(id);
	    	File zipFile;
				zipFile = new File(zipOutput(processId));
	    	
			byte[] data = Files.readAllBytes(zipFile.toPath());
			ByteArrayResource resource = new ByteArrayResource(data);
	
			// Try to determine file's content type
			String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
					.contentType(MediaType.parseMediaType(contentType))
					.contentLength(data.length)
					.body(resource);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating zip file.", null));
		}
    }
    
//    public Map<String, Object> listDirectoryFilesAndFolders( File dir ) {
//    	File[] content = dir.listFiles(); 
//
//    	List<File> files = new LinkedList<>();
//    	List<Map<String, Object>> folders = new LinkedList<>();
//
//    	for( File f : content ) {
//    		if( f.isDirectory() ) {
//    			Map<String, Object> subList = listDirectoryFilesAndFolders( f );
//    			folders.add( subList );
//    		} else {
//    				files.add( f );
//    		}
//    	}
//    	Map<String, Object> result = new HashMap<>();
//    	result.put( "folders", folders );
//    	result.put( "files", files );
//    	return result;
//    }
    
   public Map<String, Object> listDirectoryTree( File dir ) {
   	File[] content = dir.listFiles(); 

   	Map<String, Object> files = new HashMap<String, Object>();

   	for( File f : content ) {
			Map<String, Object> subList = listDirectoryTree( f );
   		files.put( f.getName(), subList );
   	}

   	return files;
   }

   public List<Map<String, Object>> listDirectoryTreeComplete( File dir ) {
   	File[] content = dir.listFiles(); 

   	List<Map<String,Object>> files = new ArrayList<Map<String, Object>>();

   	for( File f : content ) {
   		HashMap<String, Object> fileObject = new HashMap<String, Object>();
   		fileObject.put("name", f.getName());
   		fileObject.put("path", f.getAbsolutePath());
   		fileObject.put("isDirectory", f.isDirectory());
   		if(f.isDirectory()) {
       		fileObject.put("files", listDirectoryTreeComplete( f ));
   		}
   		files.add(fileObject);
   	}

   	return files;
   }

   public ArrayNode listDirectoryObjectNode( File dir ) {
   	File[] content = dir.listFiles(); 
   	
   	ObjectMapper mapper = new ObjectMapper();
       ArrayNode files = mapper.createArrayNode();

   	for( File f : content ) {
   		ObjectNode fileObject = mapper.createObjectNode();
   		fileObject.put("name", f.getName());
   		fileObject.put("path", f.getAbsolutePath());
   		fileObject.put("isDirectory", f.isDirectory());
   		if(f.isDirectory()) {
       		fileObject.set("files", listDirectoryObjectNode( f ));
   		}
   		files.add(fileObject);
   	}

   	return files;
   }
   
//   @PostMapping("/dataset/{id}/urls")
//	public List<String> listDatasetUrlsById(
//			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
//			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii", defaultValue = "dcm") 
//			@Valid @RequestParam(value = "format", required = false, defaultValue = "dcm") String format)
//			throws RestServiceException, IOException {
//
//		final Dataset dataset = datasetService.findById(datasetId);
//
//		List<URL> pathURLs = new ArrayList<URL>();
//		
//		try {
//			if ("dcm".equals(format)) {
//				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
//			} else if ("nii".equals(format)) {
//				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
//			} else {
//				throw new RestServiceException(
//						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
//			}
//		} catch (IOException e) {
//			throw new RestServiceException(
//					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while listing dataset urls.", null));
//		} 
//
//		List<String> urls = new ArrayList<String>();
//		
//		for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
//			URL url =  (URL) iterator.next();
//			urls.add(url.getPath());
//		}
//		
//		return urls;
//	}
//   
//   @PostMapping("/dataset/{id}/files")
//	public ArrayNode downloadDatasetById(
//			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
//			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii", defaultValue = "dcm") 
//			@Valid @RequestParam(value = "format", required = false, defaultValue = "dcm") String format)
//			throws RestServiceException, IOException {
//
//		final Dataset dataset = datasetService.findById(datasetId);
//		if (dataset == null) {
//			throw new RestServiceException(
//					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
//		}
//		
//		/* Create folder and file */
//		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
//		long n = RANDOM.nextLong();
//		if (n == Long.MIN_VALUE) {
//			n = 0; // corner case
//		} else {
//			n = Math.abs(n);
//		}
//		String tmpFilePath = tmpDir + File.separator + Long.toString(n);
//		File workFolder = new File(tmpFilePath + DOWNLOAD);
//		workFolder.mkdirs();
//
//		try {
//			List<URL> pathURLs = new ArrayList<URL>();
//			if ("dcm".equals(format)) {
//				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
//				downloader.downloadDicomFilesForURLs(pathURLs, workFolder);
//			} else if ("nii".equals(format)) {
//				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
//				copyNiftiFilesForURLs	copyNiftiFilesForURLs(pathURLs, workFolder);
//			} else {
//				throw new RestServiceException(
//						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
//			}
//		} catch (IOException | MessagingException e) {
//			throw new RestServiceException(
//					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
//		} 
//
//		return listDirectoryObjectNode(workFolder);
//	}
//
//	/**
//	 * Receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
//	 * @param urls
//	 * @param workFolder
//	 * @throws IOException
//	 * @throws MessagingException
//	 */
//	private void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder) throws IOException {
//		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
//			URL url =  (URL) iterator.next();
//			File srcFile = new File(url.getPath());
//			File destFile = new File(workFolder.getAbsolutePath() + File.separator + srcFile.getName());
//			Files.copy(srcFile.toPath(), destFile.toPath());
//		}
//	}
//	
//	/**
//	 * Reads all dataset files depending on the format attached to one dataset.
//	 * @param dataset
//	 * @param pathURLs
//	 * @throws MalformedURLException
//	 */
//	private void getDatasetFilePathURLs(final Dataset dataset, List<URL> pathURLs, DatasetExpressionFormat format) throws MalformedURLException {
//		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
//		for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
//			DatasetExpression datasetExpression = (DatasetExpression) itExpressions.next();
//			if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
//				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
//				for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
//					DatasetFile datasetFile = (DatasetFile) itFiles.next();
//					URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
//					pathURLs.add(url);
//				}
//			}
//		}
//	}
//	
    @PostMapping("/tool/update-database/")
    public String updateDatabase() {
    	BoutiquesUtils.updateToolDatabase();
        return "Database update started.";
    }
}
