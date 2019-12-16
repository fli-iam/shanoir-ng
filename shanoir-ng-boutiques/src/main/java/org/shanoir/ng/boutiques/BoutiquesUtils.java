package org.shanoir.ng.boutiques;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.shanoir.ng.boutiques.model.BoutiquesTool;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


@FunctionalInterface
interface SendMessage {
    public void apply(String message, boolean isError);
}

public class BoutiquesUtils {

	static final String BOUTIQUES_COMMAND = "bosh";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
//	static final String BOUTIQUES_COMMAND = "python boutiques/bosh.py";

	static boolean processStarted = false;

	public static final String USER_ID_TOKEN_ATT = "userId";
	
	/**
	 * Get current access token.
	 * 
	 * @return access token.
	 * @throws SecurityException
	 */
	@SuppressWarnings("rawtypes")
	private static KeycloakSecurityContext getKeycloakSecurityContext() throws SecurityException {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			throw new SecurityException("Anonymous user");
		}
		final KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return principal.getKeycloakSecurityContext();
	}
	
	/**
	 * Get current user id from Keycloak token.
	 * 
	 * @return user id.
	 * @throws RuntimeException
	 */
	public static Long getTokenUserId() {
		final KeycloakSecurityContext context = getKeycloakSecurityContext();
		final AccessToken accessToken = context.getToken();
		if (accessToken == null) {
			throw new RuntimeException("Access token not found");
		}
		final Map<String, Object> otherClaims = accessToken.getOtherClaims();
		if (otherClaims.containsKey(USER_ID_TOKEN_ATT)) {
			return Long.valueOf(otherClaims.get(USER_ID_TOKEN_ATT).toString());
		}
		return null;
	}

    public static String getProcessId(String toolId, String sessionId) {
		final Long userId = getTokenUserId();
		return toolId + sessionId + Long.toString(userId);
    }

    public static String getOutputPath() {
    	String outputPath = System.getenv("BOUTIQUES_OUTPUT_PATH");
    	if(outputPath == null) {
    		outputPath = System.getProperty(JAVA_IO_TMPDIR) + File.separator + "boutiques" + File.separator + "output";
    	}
    	return outputPath;
    }

    public static String getInputPath() {
    	String inputPath = System.getenv("BOUTIQUES_INPUT_PATH");
    	if(inputPath == null) {
    		inputPath = System.getProperty(JAVA_IO_TMPDIR) + File.separator + "boutiques" + File.separator + "input";
    	}
    	return inputPath;
    }

    public static String getProcessOutputPath(String processId) {
    	String outputPath = getOutputPath();
    	String processOutputPath = outputPath + File.separator + processId;
    	return processOutputPath;
    }
    
	public static String writeTemporaryFile(String pFilename, String content) throws IOException {
	    File tempDir = new File(System.getProperty(JAVA_IO_TMPDIR) + File.separator + "boutiques-tmp");
	    tempDir.mkdirs();
	    File tempFile = File.createTempFile(pFilename, ".tmp", tempDir);
	    FileWriter fileWriter = new FileWriter(tempFile, true);
	    System.out.println(tempFile.getAbsolutePath());
	    BufferedWriter bw = new BufferedWriter(fileWriter);
	    bw.write(content);
	    bw.close();
	    return tempFile.getAbsolutePath();
	}

    private static String sendStream(BufferedReader bufferedReader, boolean isError, SendMessage sendMessage) {
    	try {

	    	String line = bufferedReader.readLine();
			if(line != null) {
	            System.out.println(line);
            	sendMessage.apply(line, isError);
			}
			return line;
	    } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
        	sendMessage.apply("Server error: " + ex.getMessage(), true);
	        ex.printStackTrace();
	    }
    	return null;
    }

    public static void sendProcessStreams(Process process, SendMessage sendMessage) {
    	// Note: it is also possible not to buffer the input stream, to have more real time feedback
    	// 	     but buffer reader is convenient to send the stream line by line (otherwise one must choose an arbitrary buffer size)
        BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        System.out.println("Start reading process");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	  @Override
        	  public void run() {
        		  String inputLine = sendStream(inputBufferedReader, false, sendMessage);
        		  String errorLine = sendStream(errorBufferedReader, true, sendMessage);
        		  if(inputLine == null && errorLine == null && !process.isAlive()) {
        			  System.out.println("Process finished");
        			  timer.cancel();
        		  }
        	  }
        	}, 0, 250);

    }

//    public static void sendProcessStreams(Process process, SendMessage sendMessage) {
//    	// Daemonize the reading process just to make sure it is non blocking 
//    	// but it is not working, the output seems to be buffered 
//    	// and thrown in one shot after a relatively long period of time...
//        Thread commandLineThread = new Thread(() -> {
//        	sendProcessStreamsThreaded(process, sendMessage);
//        });
//        commandLineThread.setDaemon(true);
//        commandLineThread.start();
//    }

    public static Process runCommandLineAsync(final String cmdline, final String directory) throws IOException {
    	String[] command = { "/bin/bash", "-c", cmdline};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(directory != null ? directory : "."));
        Process process = processBuilder.start();
        return process;
    }

    public static int runCommandLineSync(final String cmdline, final String directory, ArrayList<String> output) throws IOException, Exception {
    	String[] command = { "/bin/bash", "-c", cmdline};
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(directory != null ? directory : "."));
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
        	output.add(line);
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        return exitCode;
    }
    
    public static ArrayList<BoutiquesTool> parseBoutiquesSearch(ArrayList<String> searchOutput) {
    	
        ArrayList<BoutiquesTool> searchResults = new ArrayList<BoutiquesTool>();
    	if(searchOutput.size() == 0) {
    		return searchResults;
    	}
        String headingLine = searchOutput.get(1);
        int idIndex = headingLine.indexOf("ID");
        int titleIndex = headingLine.indexOf("TITLE");
        int descriptionIndex = headingLine.indexOf("DESCRIPTION");
        int nDownloadsIndex = headingLine.indexOf("DOWNLOADS");
        
        for(int i=2 ; i<searchOutput.size() ; i++){
            String line = searchOutput.get(i);

            String id = line.substring(idIndex, titleIndex).trim();
            String name = line.substring(titleIndex, descriptionIndex).trim();
            String description = line.substring(descriptionIndex, nDownloadsIndex).trim();
            int nDownloads = Integer.parseInt(line.substring(nDownloadsIndex, line.length() - 1).trim());

            searchResults.add(new BoutiquesTool(id, name, description, nDownloads));
        }
        return searchResults;
    }
    
    private static String writeSearchResultsToJSON(ArrayList<BoutiquesTool> searchResults) {
    	
    	Path filePath = Paths.get(System.getProperty("user.home"), ".cache", "boutiques", "descriptors.json");

	    ObjectMapper mapper = new ObjectMapper();

	    try {
	    	File descriptorFile = filePath.toFile();
	    	descriptorFile.getParentFile().mkdirs();
	    	descriptorFile.createNewFile();
	        mapper.writeValue(descriptorFile, searchResults);
	    } catch (IOException e) {  
	        e.printStackTrace();
	    }
	    
	    return filePath.toString();
    }

    public static ArrayList<BoutiquesTool> searchAllTools() {

        ArrayList<BoutiquesTool> searchResults = new ArrayList<BoutiquesTool>();
        try {
        	ArrayList<String> output = new ArrayList<String>();
        	runCommandLineSync(BOUTIQUES_COMMAND + " search -m 1000", null, output);
        	searchResults = parseBoutiquesSearch(output);
        	writeSearchResultsToJSON(searchResults);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return searchResults;
    }
    
    private static ObjectNode getToolDescriptor(String id) throws JsonParseException, JsonMappingException, IOException {

        String descriptorFileName = id.replace('.', '-') + ".json";
        ObjectMapper objectMapper = new ObjectMapper();

        File file = Paths.get(System.getProperty("user.home") , ".cache", "boutiques", descriptorFileName).toFile();

        ObjectNode descriptor = objectMapper.readValue(file, ObjectNode.class);

        return descriptor;
    }

    private static void updateToolDatabaseFiles(ArrayList<BoutiquesTool> searchResults) throws JsonParseException, JsonMappingException, IOException {
	    ObjectMapper mapper = new ObjectMapper();
        final ObjectNode descriptors = mapper.createObjectNode();
        for(BoutiquesTool boutiquesTool: searchResults)
        {
        	ObjectNode descriptorObject = getToolDescriptor(boutiquesTool.getId());
            descriptorObject.put("id", boutiquesTool.getId());
            descriptorObject.put("nDownloads", boutiquesTool.getNDownloads());
            descriptors.set(boutiquesTool.getId(), descriptorObject);
        }
                
    	Path filePath = Paths.get(System.getProperty("user.home"), ".cache", "boutiques", "descriptors.json");

	    try {     
	        mapper.writeValue(filePath.toFile(), descriptors);
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
    }
    
    public static void updateToolDatabase() throws Exception {
    	ArrayList<BoutiquesTool> searchResults = searchAllTools();
    	String pullCommandLine = BOUTIQUES_COMMAND + " pull";
    	for (BoutiquesTool boutiquesTool : searchResults) {
    		pullCommandLine += " " + boutiquesTool.getId();
		}
    	
    	Process process = runCommandLineAsync(pullCommandLine, null);

        Thread commandLineThread = new Thread(() -> {
            try {
				int exitCode = process.waitFor();
				if(exitCode == 0) {
					updateToolDatabaseFiles(searchResults);
				} else {
		            System.out.println("Error: bosh process ended with exit code " + exitCode);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
        });
        
        commandLineThread.start();
    }

    
    public static void createDirectory(String path) {
		final File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
    }
    
    public static void setInputPaths(ObjectNode descriptor, ObjectNode invocation, String inputPath) {
    	ArrayNode inputs = (ArrayNode) descriptor.get("inputs");
        for (int i = 0; i < inputs.size(); i++) {
            JsonNode input = inputs.get(i);
            String id = input.get("id").asText();
            if(input.has("type") && input.get("type").asText().contentEquals("File") && invocation.has(id)) {
            	String filePath = invocation.get(id).asText();
            	invocation.put(id, inputPath + File.separator + filePath);
            }
        }
    }

    public static void checkOutputPaths(ObjectNode descriptor, ObjectNode invocation) throws ResponseStatusException {
    	ArrayNode inputs = (ArrayNode) descriptor.get("inputs");
    	// See the description of how the output files are generated: "Boutiques: a flexible framework for automatedapplication integration in computing platforms"
	    //                                                            https://arxiv.org/pdf/1711.09713.pdf

	    // For all inputs: if the parameter is a File or a String and has a "value-key":
	    //      check if an "output-file" has a "path-template" containing this "value-key",
	    //      remove all "path-template-stripped-extensions" from the input parameter value (which is a file name),
	    //      then replace this "value-key" in the "path-template" with the file name (= the input parameter value)
	    //      check that the resulting output paths are not absolute and do not contain double dot symbols (../)			
        for (int i = 0; i < inputs.size(); i++) {
            JsonNode inputObject = inputs.get(i);
            String inputId = inputObject.get("id").asText();
            
	        if(!inputObject.has("type")) {
	        	continue;
	        }
	        String inputType = inputObject.get("type").asText();
	        
	        if((inputType.contentEquals("File") || inputType.contentEquals("String")) && invocation.has(inputId)) {
	            // For all output files: check if one has "path-template" containing the "value-key" of the current input
	            String fileName = invocation.get(inputId).asText();
	            
				ArrayNode outputFiles = (ArrayNode) descriptor.get("output-files");
	            
	    	    for (int j = 0; j < outputFiles.size(); j++) {

	                JsonNode outputFilesDescription = outputFiles.get(j);
	                String pathTemplate = outputFilesDescription.get("path-template").asText();

	                // If the input is a File, remove the "path-template-stripped-extensions"
	                if(inputType.contentEquals("File") && outputFilesDescription.has("path-template-stripped-extensions")) {
	                	ArrayNode pathTemplateStrippedExtensions = (ArrayNode) outputFilesDescription.get("path-template-stripped-extensions");

	                    for (int k = 0 ; k<pathTemplateStrippedExtensions.size() ; ++k) {
	                        String pathTemplateStrippedExtension = pathTemplateStrippedExtensions.get(k).asText();
	                        fileName.replace(pathTemplateStrippedExtension, "");
	                    }
	                }

	                String valueKey = inputObject.get("value-key").asText();
	                
	                if(pathTemplate.contains(valueKey)) {
	                    // If the current output file has a "path-template" containing the current input "value-key": replace the "value-key" by the file name (!input value)
	                	pathTemplate = pathTemplate.replace(valueKey, fileName);

	                    // Make sure the path is not absolute and does not contain ../
	                    if(pathTemplate.contains("../"))
	                    {
	                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Output paths must not contain double-dot symbols (../).");
	                    }
	                    if(pathTemplate.startsWith("/")) {
	                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Output paths must not be absolute.");
	                    }
	                }
	            }
	        }
	    }
    }
    
	private static void zip(String sourceDirPath, String zipFilePath) throws IOException {
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
	
	public static String zipOutput(String processId) throws IOException {
    	String outputPath = BoutiquesUtils.getProcessOutputPath(processId);
    	String outputPathZip = outputPath + ".zip";
    	File file = new File(outputPathZip);
    	file.deleteOnExit();
    	zip(outputPath, outputPathZip);
    	return outputPath + ".zip";
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
}
