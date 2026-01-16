package org.shanoir.ng.vip.execution.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExecutionTrackingServiceImpl implements ExecutionTrackingService {

    @Value("${vip-data-folder}")
    private String trackingFilePrefixe;

    private int MAX_LAST_LINES_TO_CHECK = 10;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTrackingServiceImpl.class);

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public enum execStatus {VALID, SENT}

    public void updateTrackingFile(ExecutionMonitoring executionMonitoring, execStatus execStatus) {
        try {
            File trackingFile = new File(getTrackingFilePath(executionMonitoring));
            createTrackingFile(trackingFile);

            switch (execStatus) {
                case VALID -> createTrackingLine(executionMonitoring, trackingFile);
                case SENT -> updateTrackingLine(executionMonitoring, trackingFile);
                default -> throw new ShanoirException("Invalid execution status, can not track.");
            }
        } catch (IOException | ShanoirException e) {
            LOG.error("An error occured while trying to write in VIP tracking file", e);
        }
    }

    public void completeTracking(ExecutionMonitoring executionMonitoring, DatasetProcessing newProcessing) {
        try {
            File trackingFile = new File(getTrackingFilePath(executionMonitoring));
            List<String> lastLines = getLastLines(trackingFile);

            boolean retrievedLine = false;
            for (String line : lastLines) {
                List<String> lineParts = new ArrayList<>(Arrays.asList(line.split(",")));

                if(Long.parseLong(lineParts.get(1)) == executionMonitoring.getId()) {
                    lineParts.set(1, newProcessing.getId().toString());
                    lineParts.add(newProcessing.getOutputDatasets().stream().anyMatch(file -> Objects.equals("error.yaml", file.getName())) ? "true" : "false");
                    lineParts.add(newProcessing.getOutputDatasets().stream().anyMatch(file -> Objects.equals("results.yaml", file.getName())) ? "true" : "false");

                    lastLines.set(lastLines.indexOf(line), String.join(",", lineParts));
                    retrievedLine = true;
                    break;
                }
            }
            if (!retrievedLine) {
                throw new ShanoirException("Execution monitoring tracking line is lost, can not complete line.");
            }

            writeLastLines(lastLines, trackingFile);
        } catch (IOException | ShanoirException e) {
            LOG.error("An error occured while trying to write in VIP tracking file", e);
        }
    }

    /**
     * Create a new line for the execution input
     */
    private void createTrackingLine(ExecutionMonitoring executionMonitoring, File trackingFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(trackingFile,true));) {

            String newLine;

            writer.newLine();
            newLine = LocalDateTime.now().format(formatter) + ",";
            newLine += executionMonitoring.getId() + ",";
            newLine += executionMonitoring.getInputDatasets().getFirst().getDatasetAcquisition().getExamination().getId() + ",";
            newLine += executionMonitoring.getInputDatasets().stream().map(dataset -> String.valueOf(dataset.getId())).reduce((id1, id2) -> id1 + " / " + id2).orElse("") + ",";
            String names = executionMonitoring.getInputDatasets().stream().filter(dataset -> Objects.nonNull(dataset.getOriginMetadata())).map(Dataset::getName).reduce((id1, id2) -> id1 + " / " + id2).orElse("");
            newLine += (names.length() > 66 ? names.substring(0, 66) : names ) + ",,,";

            writer.write(newLine);
        } catch (IOException e) {
            LOG.error("An error occured while trying to create a line in VIP tracking file", e);
        }
    }

    /**
     * Update the execution monitoring line (at VIP sending moment)
     */
    private void updateTrackingLine(ExecutionMonitoring executionMonitoring, File trackingFile) throws IOException, ShanoirException {
        List<String> lastLines = getLastLines(trackingFile);

        boolean retrievedLine = false;
        for (String line : lastLines) {
            List<String> lineParts = new ArrayList<>(Arrays.asList(line.split(",")));

            if (Long.parseLong(lineParts.get(1)) == executionMonitoring.getId()) {
                lineParts.add("true,,");
                lastLines.set(lastLines.indexOf(line), String.join(",", lineParts));
                retrievedLine = true;
                break;
            }
        }
        if (!retrievedLine) {
            throw new ShanoirException("Execution monitoring tracking line is lost, can not update line.");
        }
        writeLastLines(lastLines, trackingFile);
    }

    /**
     * Get the n last lines of a file
     */
    private List<String> getLastLines(File trackingFile) throws IOException {
        RandomAccessFile file = new RandomAccessFile(trackingFile, "r");
        long fileLength = file.length();

        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        long pointer = fileLength - 1;

        while (pointer >= 0 && lines.size() < MAX_LAST_LINES_TO_CHECK) {
            file.seek(pointer);
            char c = (char) file.readByte();
            if (c == '\n' && pointer != fileLength - 1) {
                lines.addFirst(currentLine.reverse().toString());
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
            pointer--;
        }

        file.close();
        return lines;
    }

    /**
     * Rewrite the lines at the end of the files according to MAX_LAST_LINES_TO_CHECK
     */
    private synchronized void writeLastLines(List<String> lastLines, File trackingFile) {
        List<String> lines = null;
        try {
            //BufferedWriter clear file, so we need to read it before opening buffer
            lines = Files.readAllLines(trackingFile.toPath());
        } catch (IOException e) {
            LOG.error("An error occured while reading files in VIP tracking file", e);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(trackingFile));) {
            List<String> updatedLines = lines.subList(0, Math.max(1, lines.size() - MAX_LAST_LINES_TO_CHECK));
            updatedLines.addAll(lastLines);

            for(String line : updatedLines.subList(0, updatedLines.size() - 1)) {
                writer.write(line);
                writer.newLine();
            }
            writer.write(updatedLines.getLast());
        } catch (IOException e) {
            LOG.error("An error occured while updating a line in VIP tracking file", e);
        }
    }

    /**
     * Get the path of the tracking file relative to pipeline linked to the execution monitoring
     */
    private String getTrackingFilePath(ExecutionMonitoring executionMonitoring) {
        String pipelineName = executionMonitoring.getPipelineIdentifier().replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        return trackingFilePrefixe + "/" + pipelineName + ".csv";
    }

    /**
     * Create the tracking file relative to pipeline linked to the execution monitoring if not existing
     */
    private void createTrackingFile(File trackingFile) throws IOException {
        new File(trackingFilePrefixe).mkdirs();
        if(trackingFile.createNewFile()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(trackingFile));) {
                String headers = "Date (HH:mm dd/MM/yyyy),Processing_id,Exam_id,Dataset_id,Dataset_name,Sent_to_VIP,Error_file,Result_file";
                writer.write(headers);
            } catch (IOException e) {
                LOG.error("An error occured while creating VIP tracking file", e);
            }
        }
    }
}
