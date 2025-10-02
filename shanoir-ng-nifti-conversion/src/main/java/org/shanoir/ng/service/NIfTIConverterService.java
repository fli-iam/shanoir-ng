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

package org.shanoir.ng.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.shanoir.ng.model.Dataset;
import org.shanoir.ng.model.NiftiConverter;
import org.shanoir.ng.utils.ShanoirExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The NIfTIConverter does the actual conversion of dcm to nii files. To use the
 * converter the dcm files have to be put in separate folders.
 *
 * 1) all images for one serie are moved into /SERIES/{seriesID} and 2) all
 * images are concerning the acquisitionNumber, echoNumbers and the
 * imageOrientationPatient informations moved into /dataset{index} folders.
 *
 * Inside each dataset folder the nii conversion is called.
 *
 * @author mkain
 *
 */
@Service
public class NIfTIConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(NIfTIConverterService.class);
    public static final String DICOM_TO_NIFTI_METHOD = "to-nifti";
    public static final String BRUKER_TO_DICOM_METHOD = "to-dicom";

    @Autowired
    private ShanoirExec shanoirExec;

    /** Output files mapped by series UID. */
    private HashMap<String, List<String>> outputFiles = new HashMap<>();

    private Random rand = new Random();

    public void brukerToDicomExec(String inputFolder, String outputFolder) {
        shanoirExec.dicomifier(inputFolder, outputFolder,  NiftiConverter.DICOMIFIER.getPath(), BRUKER_TO_DICOM_METHOD);
    }

    public boolean animaToNiftiExec(String imagePathToConvert) {
        return shanoirExec.anima(imagePathToConvert);
    }

    /**
     * Execute the Nifti conversion
     *
     * @param converter
     * @param input     folder
     * @param output    folder
     *
     */
    public boolean convertToNiftiExec(Long converterId, String inputFolder, String outputFolder) {
        if (converterId == null) {
            return false;
        }

        NiftiConverter converter = NiftiConverter.getType(Math.toIntExact(converterId));
        String conversionLogs = "";

        switch (converter) {
            case MCVERTER_2_0_7:
            case MCVERTER_2_1_0:
                conversionLogs += shanoirExec.mcverterExec(inputFolder, converter.getPath(), outputFolder, true);
                break;
            case DICOMIFIER:
                conversionLogs += shanoirExec.dicomifier(inputFolder, outputFolder, NiftiConverter.DICOMIFIER.getPath(), DICOM_TO_NIFTI_METHOD);
                break;
            case MRICONVERTER:
                conversionLogs += shanoirExec.mriConverter(inputFolder, outputFolder, NiftiConverter.MRICONVERTER.getPath());
                break;
            default:
                conversionLogs += shanoirExec.dcm2niiExec(inputFolder, converter.getPath(), outputFolder, true);
                break;
        }
        // Here we should check logs to check which converter failed or not.
        LOG.error(conversionLogs);

        return !conversionLogs.contains("an error has probably occured");
    }

    /**
     * Remove unused files that are created during the conversion process.
     */
    private void removeUnusedFiles() {
        final List<File> toBeRemovedList = new ArrayList<>();
        for (final List<String> listPath : outputFiles.values()) {
            for (final String path : listPath) {
                File file = new File(path);
                if (file.getName().startsWith("o") || file.getName().startsWith("x")) {
                    toBeRemovedList.add(file);
                }
            }
        }
        for (final File toBeRemovedFile : toBeRemovedList) {
            // TODO : ne marche pas
            outputFiles.remove(toBeRemovedFile);
            boolean success = toBeRemovedFile.delete();
            if (!success) {
                LOG.error("removeUnusedFiles : error while deleting {}", toBeRemovedFile);
            }
        }
    }

    /**
     * This method is needed to identify generated nifti files in the middle of dcm
     * files.
     *
     * @return List of nifti files
     */
    public List<File> niftiFileSorting(List<File> existingFiles, File directory, File serieIDFolderFile) {
        // If one of the output files is a prop file, there has been an error
        List<File> niftiFileResult = null;
        if (outputFiles.get(serieIDFolderFile.getName()) != null) {
            List<File> niiFiles = diff(existingFiles, directory.getPath());
            niftiFileResult = niiFiles;
            if (!containsPropFile(niiFiles)) {
                for (File niiFile : niiFiles) {
                    outputFiles.get(serieIDFolderFile.getName()).add(niiFile.getAbsolutePath());
                    LOG.debug("Path niiFile : {}", niiFile.getAbsolutePath());
                }
            }
        } else {
            List<String> niiPathList = new ArrayList<>();
            if (!containsPropFile(diff(existingFiles, directory.getPath()))) {
                List<File> niiFileList = diff(existingFiles, directory.getPath());
                niftiFileResult = niiFileList;
                for (File niiFile : niiFileList) {
                    niiPathList.add(niiFile.getAbsolutePath());
                    LOG.debug("Path niiFile : {}", niiFile.getAbsolutePath());
                }
                outputFiles.put(serieIDFolderFile.getName(), niiPathList);
            }
        }
        // delete the unused files
        removeUnusedFiles();
        return niftiFileResult;
    }

    /**
     * adapt to generated folders by dicom2nifti converter
     *
     * @param existingFiles the currently existing files in the directory
     * @param directory     the import directory
     * @param dataset       the dataset we are importing
     * @return
     */
    public List<File> niftiFileSortingDicom2Nifti(List<File> existingFiles, File directory, Dataset dataset) {
        // Have to adapt to generated folders by dicom2nifti converter
        List<File> niiFiles = new ArrayList<>(FileUtils.listFiles(directory,
                new RegexFileFilter("^(.*?)\\.(nii|json|nii.gz)"), DirectoryFileFilter.DIRECTORY));

        for (File file : niiFiles) {
            try {
                // Copy all nifti files
                Files.copy(file.toPath(), Paths.get(directory.getPath() + File.separator + rand.nextInt()
                        + dataset.getName() + "_" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Error while copying files", e);
            }
        }

        return diff(existingFiles, directory.getPath()).stream().filter(file -> file.isFile())
                .collect(Collectors.toList());
    }

    /**
     * Make a diff to know which files from destinationFolder are not in the given
     * list of files.
     *
     * @param existingFiles     the existing files
     * @param destinationFolder the destination folder
     *
     * @return the list< file>
     */
    private List<File> diff(final List<File> existingFiles, final String destinationFolder) {
        final List<File> resultList = new ArrayList<>();
        final List<File> outputFilesToDiff = Arrays.asList(new File(destinationFolder).listFiles());
        for (final File file : outputFilesToDiff) {
            if (!existingFiles.contains(file)) {
                resultList.add(file);
            }
        }
        return resultList;
    }

    /**
     * Check if the newly created nifti files list contains a .prop file If it is
     * the case, then there has been a problem during conversion and should be
     * considered as failed.
     *
     */
    private boolean containsPropFile(List<File> niftiFiles) {
        for (File current : niftiFiles) {
            if (current.getPath().contains(".prop")) {
                return true;
            }
        }
        return false;
    }
}
