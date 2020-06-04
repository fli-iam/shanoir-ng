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

package org.shanoir.ng.importer.dcm2nii;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.importer.model.Dataset;
import org.shanoir.ng.importer.model.DatasetFile;
import org.shanoir.ng.importer.model.DiffusionGradient;
import org.shanoir.ng.importer.model.EchoTime;
import org.shanoir.ng.importer.model.ExpressionFormat;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.DiffusionUtil;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.ShanoirExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * The NIfTIConverter does the actual conversion of dcm to nii files.
 * To use the converter the dcm files have to be put in separate folders.
 * 
 * 1) all images for one serie are moved into /SERIES/{seriesID} and
 * 2) all images are concerning the acquisitionNumber, echoNumbers and the
 * imageOrientationPatient informations moved into /dataset{index} folders.
 * 
 * Inside each dataset folder the nii conversion is called.
 * 
 * @author mkain
 *
 */
@Service
public class DatasetsCreatorAndNIfTIConverterService {

	private static final String BVAL = ".bval";

	private static final String BVEC = ".bvec";

	private static final String DATASET_STR = "dataset";

	private static final Logger LOG = LoggerFactory.getLogger(DatasetsCreatorAndNIfTIConverterService.class);

	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";

	private static final String SERIES = "SERIES";

	@Autowired
	private NIfTIConverterRepository niftiConverterRepository;

	@Autowired
	private ShanoirExec shanoirExec;
	
	@Value("${shanoir.import.series.seriesProperties}")
	private String seriesProperties;

	@Value("${shanoir.import.series.donotseparatedatasetsinserie}")
	private String doNotSeparateDatasetsInSerie;

	@Value("${shanoir.conversion.converters.convertwithclidcm}")
	private String convertWithClidcm;
	
	@Value("${shanoir.conversion.converters.path}")
	private String convertersPath;
	
	@Value("${shanoir.conversion.dcm2nii.converters.convertas4d}")
	private String convertAs4D;

	@Value("${shanoir.conversion.dcm2nii.converters.path.linux}")
	private String convertersPathLinux;

	@Value("${shanoir.conversion.dcm2nii.converters.path.windows}")
	private String convertersPathWindows;

	@Value("${shanoir.conversion.dcm2nii.converters.clidcm.path.linux}")
	private String clidcmPathLinux;

	@Value("${shanoir.conversion.dcm2nii.converters.clidcm.path.windows}")
	private String clidcmPathWindows;

	/** Logs of the conversion. */
	private String conversionLogs;

	/** Output files mapped by series UID. */
	private HashMap<String, List<String>> outputFiles = new HashMap<>();

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public NIfTIConverter findById(Long id) {
		return niftiConverterRepository.findOne(id);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public List<NIfTIConverter> findAll() {
		return niftiConverterRepository.findAll().stream().filter(converter -> converter.getIsActive()).collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public void createDatasetsAndRunConversion(Patient patient, File workFolder, Long converterId) throws ShanoirException {
		File seriesFolderFile = new File(workFolder.getAbsolutePath() + File.separator + SERIES);
		if(!seriesFolderFile.exists()) {
			seriesFolderFile.mkdirs();
		} else {
			throw new ShanoirException("Error while creating series folder: folder already exists.");
		}
		conversionLogs = "";
		List<Study> studies = patient.getStudies();
		for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
			Study study = studiesIt.next();
			List<Serie> series = study.getSeries();
			for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
				Serie serie = seriesIt.next();
				if (serie.getSelected()) {
					File serieIDFolderFile = createSerieIDFolderAndMoveFiles(workFolder, seriesFolderFile, serie);
					boolean serieIdentifiedForNotSeparating;
					try {
						serieIdentifiedForNotSeparating = checkSerieForPropertiesString(serie, seriesProperties);
						// if the serie is not one of the series, that should not be separated, please separate the series,
						// otherwise just do not separate the series and keep all images for one nii conversion
						serie.setDatasets(new ArrayList<Dataset>());
						constructDicom(serieIDFolderFile, serie, serieIdentifiedForNotSeparating);
						constructNifti(serieIDFolderFile, serie, converterId);
					} catch (NoSuchFieldException | SecurityException e) {
						LOG.error(e.getMessage());
					}
					// as images/non-images are migrated to datasets, clear the list now
					serie.getImages().clear();
					serie.getNonImages().clear();
				}
			}
		}
	}

	/**
	 *
	 * Create the nifti dataset expression
	 * 
	 **/
	private ExpressionFormat generateNiftiDatasetExpression(Dataset dataset,Serie serie) {
		LOG.debug("Create the nifti dataset expressions for dataset : {} in serie {}", dataset.getName(), serie.getSequenceName());
		final ExpressionFormat datasetExpressionNifti = new ExpressionFormat();
		datasetExpressionNifti.setType("nii");
		return datasetExpressionNifti;
	}

	/**
	 *
	 * Create the nifti dataset expressions files
	 * 
	 **/
	private void generateNiftiDatasetFiles(ExpressionFormat datasetExpressionFormat, Dataset dataset,List<File> niftiFileList) {
		if (niftiFileList != null  && !niftiFileList.isEmpty()) {
			for (final File niftiFile : niftiFileList) {
				LOG.debug("create DatasetFile : processing the file {}", niftiFile.getName());
				final DatasetFile niftiDatasetFile = new DatasetFile();
				niftiDatasetFile.setPath(niftiFile.toURI().toString().replaceAll(" ", "%20"));
				datasetExpressionFormat.getDatasetFiles().add(niftiDatasetFile);
			}

			// if necessary, rename the bvec and bval files (for DTI)
			renameBvecBval(datasetExpressionFormat);

			/*
			 * if there was some DTI images, then we can now use the
			 * bvec and bval files to create the diffusion gradients and
			 * add them to the MR Protocol. Indeed, it is more likely to
			 * do so now because extracting the diffusion gradients from
			 * the dicom files is tricky.
			 */
			extractDiffusionGradients(dataset,datasetExpressionFormat);
		}
	}


	/**
	 * Extract from the bvec and bval files the diffusion gradients and fullfill
	 * the mr protocol.
	 *
	 * @param mrProtocol
	 *            the mr protocol
	 * @param mrDataset
	 *            the mr dataset
	 * @param datasetExpressionNifti
	 *            the dataset expression nifti
	 */
	private void extractDiffusionGradients(Dataset dataset, ExpressionFormat datasetExpressionNifti) {
		LOG.debug("extractDiffusionGradients : Begin");
		if (datasetExpressionNifti != null) {
			try {
				boolean bvecOrBvalFound = false;
				for (final DatasetFile datasetFile : datasetExpressionNifti.getDatasetFiles()) {
					final File file = new File(new URI(datasetFile.getPath()));
					if (file.getName().endsWith(BVEC) || file.getName().endsWith(BVAL)) {
						bvecOrBvalFound = true;
						break;
					}
				}

				if (bvecOrBvalFound) {
					String[] xValues = null;
					String[] yValues = null;
					String[] zValues = null;
					String[] bValues = null;
					for (final DatasetFile datasetFile : datasetExpressionNifti.getDatasetFiles()) {
						final File file = new File(new URI(datasetFile.getPath()));
						if (file.getName().endsWith(BVEC)) {
							try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
								String line = null;
								List<String> items = new ArrayList<>();
								while ((line = reader.readLine()) != null) {
									items.add(line);
								}
								xValues = items.get(0).split("\\s");
								yValues = items.get(1).split("\\s");
								zValues = items.get(2).split("\\s");
							}
						}
						if (file.getName().endsWith(BVAL)) {
							try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
								String line = null;
								List<String> items = new ArrayList<>();
								while ((line = reader.readLine()) != null) {
									items.add(line);
								}
								bValues = items.get(0).split("\\s");
							}
						}
					}

					if (xValues != null && yValues != null && zValues != null && bValues != null) {
						if (xValues.length == yValues.length && yValues.length == zValues.length
								&& zValues.length == bValues.length) {
							for (int i = 0; i < xValues.length; i++) {
								final DiffusionGradient diffusionGradient = new DiffusionGradient();
								diffusionGradient.setDiffusionGradientBValue(Double.valueOf(bValues[i]));
								diffusionGradient.setDiffusionGradientOrientationX(Double.valueOf(xValues[i]));
								diffusionGradient.setDiffusionGradientOrientationY(Double.valueOf(yValues[i]));
								diffusionGradient.setDiffusionGradientOrientationZ(Double.valueOf(zValues[i]));
								dataset.getDiffusionGradients().add(i, diffusionGradient);
								LOG.debug("extractDiffusionGradients : adding diffusion gradient {} dataset {}", diffusionGradient,  dataset.getName());
							}
						} else {
							LOG.error("extractDiffusionGradients : The matrices doesn't have the same size!!");
						}
					} else {
						LOG.error("extractDiffusionGradients : error occured when getting the b-vector and b-values");
					}
				}
			} catch (final URISyntaxException | IOException exc) {
				LOG.error("extractDiffusionGradients : {}", exc);
			}
		}
		LOG.debug("extractDiffusionGradients : End");
	}

	/**
	 * Execute the Nifti conversion
	 *
	 * @param converter
	 * @param input folder
	 * @param output folder
	 * @param boolean is a 4D volume
	 * @param boolean is convert to clidcm
	 *
	 */
	private void convertToNiftiExec(NIfTIConverter converter, String inputFolder, String outputFolder, boolean is4D) {
		String converterPath = convertersPath + converter.getName();
		// Mcverter
		if (converter != null && converter.isMcverter()) {
			is4D = true;
			conversionLogs += shanoirExec.mcverterExec(inputFolder, converterPath, outputFolder, is4D);
			// Clidcm
		} else if (converter != null && converter.isClidcm()) {
			try {
				conversionLogs += shanoirExec.clidcmExec(inputFolder, converterPath, outputFolder);
			} catch (Exception e) {
				LOG.debug("Error converting with clidcm outputfolder : " + outputFolder + " - is4D :" + is4D + " - ", e);
			}
			/*
			 * Some '.prop' files may have been created. We want to convert the mas '.bvec'
			 * and '.bval' files because this is the type of files normally created by
			 * dcm2nii .
			 */
			createBvecAndBval(outputFolder);
			// Dicom2Nifti
		} else if (converter != null && converter.isDicom2Nifti()) {
			conversionLogs += shanoirExec.dicom2niftiExec(inputFolder, converterPath, outputFolder);
			// dcm2nii
		} else {
			is4D = true;
			conversionLogs += shanoirExec.dcm2niiExec(inputFolder, converterPath, outputFolder, is4D);
		}
	}

	/**
	 * Search for a '.prop' file. If found, then creates a '.bvec' and a '.bval'
	 * file from the '.prop' file.
	 *
	 * @param path
	 */
	private List<File> createBvecAndBval(final String path) {
		LOG.debug("createBvecAndBval : Begin, params : path={}", path);
		List<File> bvecAndBval = new ArrayList<>();
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return FilenameUtils.isExtension(name, new String[] { "prop", "PROP" });
			}
		};
		final String[] propFiles = new File(path).list(filter);
		if (propFiles != null && propFiles.length != 0) {
			for (final String propFile : propFiles) {
				LOG.debug("createBvecAndBval : '.prop' file found : {}", propFile);
				final List<File> thisList = DiffusionUtil.propToBvecBval(new File(path, propFile), new File(path));
				LOG.debug("createBvecAndBval : bvec and bvals created : {}", thisList);
				bvecAndBval.addAll(thisList);
			}
		}
		if(bvecAndBval.isEmpty()) {
			conversionLogs += "There was an error creating bvec and bval. DiffusionGradientOrientation and/or B0 values may be missing in DICOM file.";
		}
		LOG.debug("createBvecAndBval : end");
		return bvecAndBval;
	}

	/**
	 * Sometimes, dcm2nii creates files named '.bvec' and '.bval'. This methods
	 * renames them with the name of the dataset.
	 *
	 * @param datasetExpressionNifti
	 *            the dataset expression nifti
	 */
	private void renameBvecBval(final ExpressionFormat datasetExpressionNifti) {
		LOG.debug("renameBvecBval : Begin");
		if (datasetExpressionNifti != null) {
			try {
				boolean toBeRenamed = false;
				for (final DatasetFile datasetFile : datasetExpressionNifti.getDatasetFiles()) {
					final File file = new File(new URI(datasetFile.getPath()));
					if (BVEC.equalsIgnoreCase(file.getName()) || BVAL.equalsIgnoreCase(file.getName())) {
						toBeRenamed = true;
						break;
					}
				}
				if (toBeRenamed) {
					LOG.debug("renameBvecBval : .bvec and .bval files to rename");
					String name = null;
					for (final DatasetFile datasetFile : datasetExpressionNifti.getDatasetFiles()) {
						final File file = new File(new URI(datasetFile.getPath()));
						if (file.getName().endsWith(".nii")) {
							name = file.getName().substring(0, file.getName().lastIndexOf(".nii"));
						}
						if (file.getName().endsWith(".nii.gz")) {
							name = file.getName().substring(0, file.getName().lastIndexOf(".nii.gz"));
						}
					}

					if (name != null) {
						for (final DatasetFile datasetFile : datasetExpressionNifti.getDatasetFiles()) {
							final File file = new File(new URI(datasetFile.getPath()));
							if (BVEC.equalsIgnoreCase(file.getName())) {
								final String newName = name + BVEC;
								final String newPath = datasetFile.getPath().replaceAll(BVEC, newName);
								file.renameTo(new File(new URI(newPath)));
								datasetFile.setPath(newPath);
								LOG.debug("renameBvecBval : .bvec renamed to {}", newName);
							} else if (BVAL.equalsIgnoreCase(file.getName())) {
								final String newName = name + BVAL;
								final String newPath = datasetFile.getPath().replaceAll(BVAL, newName);
								file.renameTo(new File(new URI(newPath)));
								datasetFile.setPath(newPath);
								LOG.debug("renameBvecBval : .bval renamed to {}", newName);
							}
						}
					}
				} else {
					LOG.debug("renameBvecBval : no file to rename");
				}
			} catch (final URISyntaxException exc) {
				LOG.error("renameBvecBval : ", exc);
			}
		}
		LOG.debug("renameBvecBval : End");
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
	 * This method receives a serie object and a String from the properties
	 * and checks if the tag exists with a specific value.
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private boolean checkSerieForPropertiesString(final Serie serie, final String propertiesString) throws NoSuchFieldException {
		final String[] itemArray = propertiesString.split(SEMI_COLON);
		for (final String item : itemArray) {
			final String tag = item.split(DOUBLE_EQUAL)[0];
			final String value = item.split(DOUBLE_EQUAL)[1];
			LOG.debug("checkDicomFromProperties : tag={}, value={}", tag, value);
			try {
				Class<? extends Serie> aClass = serie.getClass();
				Field field = aClass.getDeclaredField(tag);
				field.setAccessible(true);
				String dicomValue = (String) field.get(serie);
				String wildcard = ImportUtils.wildcardToRegex(value);
				if (dicomValue != null && dicomValue.matches(wildcard)) {
					return true;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getMessage());
			}
		}
		return false;
	}


	/**
	 * This method does the actual conversion on calling the converter, either on the level
	 * of the serie folder or on the level of each dataset folder inside the serie folder.
	 * Attention: this code does not take care of spectroscopy aspects. There should be a
	 * separate import code for this data.
	 * 
	 * In Shanoir old implementation the outputFiles is never used afterwards in the import
	 * (only for cleaning after the import has finished). In MrDatasetAcquisitionHome in the
	 * method getDicomFiles for each dataset0,1,.. folder all files are listed, when they are
	 * in listed in the serie meta-data they are considered as dicom, all others as nii files.
	 * It would be much better to add this infos to the json from here and just use them after?
	 * 
	 * @todo the conversion progress needs to be send to the frontend to be displayed
	 * 
	 * @param serieIDFolderFile
	 * @param serie
	 * @param seriesCounter
	 * @param numberOfSeries
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private NIfTIConverter datasetToNiftiConversionLauncher(Dataset dataset, File directory, Serie serie, Long converterId, boolean isConvertAs4D, boolean isConvertWithClidcm) throws NoSuchFieldException, SecurityException {

		// search for the existing files in the destination folder

		LOG.info("convertToNifti : create nifti files for the dataset : {}", dataset.getName());
		if (conversionLogs != null && !"".equals(conversionLogs)) {
			conversionLogs += "\n";
		} else {
			conversionLogs = "";
		}


		NIfTIConverter converter = findById(converterId);
		convertToNiftiExec(converter, directory.getPath(), directory.getPath(), isConvertAs4D);
		LOG.info("conversionLogs : {}", conversionLogs);
		return converter;

	}


	/**
	 * This method is needed to identify generated nifti files in the middle of dcm files.
	 * 
	 * @return List of nifti files
	 */

	private List<File> niftiFileSorting(List<File> existingFiles, File directory, File serieIDFolderFile) {
		// If one of the output files is a prop file, there has been an error
		List<File> niftiFileResult = null;
		if (outputFiles.get(serieIDFolderFile.getName()) != null) {
			List<File> niiFiles = diff(existingFiles, directory.getPath());
			niftiFileResult = niiFiles;
			if (!containsPropFile(niiFiles)) {
				for (File niiFile : niiFiles) {
					outputFiles.get(serieIDFolderFile.getName()).add(niiFile.getAbsolutePath());
					LOG.info("Path niiFile : {}", niiFile.getAbsolutePath());
				}
			}
		} else {
			List<String> niiPathList = new ArrayList<>();
			if (!containsPropFile(diff(existingFiles, directory.getPath()))) {
				List<File>  niiFileList = diff(existingFiles, directory.getPath());
				niftiFileResult = niiFileList;
				for (File niiFile : niiFileList) {
					niiPathList.add(niiFile.getAbsolutePath());
					LOG.info("Path niiFile : {}", niiFile.getAbsolutePath());
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
	 * @param converter
	 * @param niiFiles
	 * @param directory
	 * @return
	 */
	private List<File> niftiFileSortingDicom2Nifti(NIfTIConverter converter, List<File> niiFiles, File directory) {
		// Have to adapt to generated folders by dicom2nifti converter
		if (converter.isDicom2Nifti()) {
			List<File> existingFiles = Arrays.asList(directory.listFiles());
			// copy all files into the directory
			for (File niiFile : niiFiles) {
				ImportUtils.copyAllFiles(niiFile, directory);
			}
			// delete folder hierarchy created by dicomifier
			for (File niiFile : niiFiles) {
				try {
					if (niiFile.isDirectory()) {
						FileUtils.deleteDirectory(niiFile);
					} else {
						niiFile.delete();
					}
				} catch (Exception e) {
					LOG.error("Error while deleting dicom2nifti generated folder {}", e.getMessage());
				}
			}
			// nii files are the diff
			niiFiles = diff(existingFiles, directory.getPath());
		}
		return niiFiles;
	}


	/**
	 * This method generates the nifti files of serie  in proper datasets for an entire serie.
	 * It also constructs the associated Nifti ExpressionFormat and DatasetFiles within the Dataset object.
	 * Finally it also constructs the Bvec and BVal values needed for Diffusion and store them in a a list of Diffusion Gradient which is hold by the dataset itself.
	 *
	 * @param serieIDFolderFile
	 * @param serie
	 * @param serieIdentifiedForNotSeparating
	 * @param convertedId
	 * @throws NoSuchFieldException
	 * 
	 */
	private void constructNifti(File serieIDFolderFile, final Serie serie, Long converterId) throws NoSuchFieldException {

		LOG.debug("convertToNifti : create nifti files for the serie : {}", serieIDFolderFile.getAbsolutePath());

		if (serie != null) {
			boolean isConvertAs4D=false;
			boolean isConvertWithClidcm=false;
			try {
				isConvertAs4D = checkSerieForPropertiesString(serie, seriesProperties);
				isConvertWithClidcm = checkSerieForPropertiesString(serie, convertWithClidcm);
			} catch (NoSuchFieldException | SecurityException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			if (serie.getDatasets().size() > 1 ) {
				// Need to construct nifti files for each dataset in current serie
				int index = 0;
				for (Dataset dataset : serie.getDatasets()) {
					File directory = new File(serieIDFolderFile + File.separator + DATASET_STR + index);
					if (directory.isDirectory()) {
						LOG.info("convertToNifti : create nifti files for the dataset {} in directory : {}", dataset.getName(), directory.getName());
						final List<File> existingFiles = Arrays.asList(directory.listFiles());
						NIfTIConverter converter = null;
						try {
							converter = datasetToNiftiConversionLauncher(dataset, directory, serie, converterId, isConvertAs4D, isConvertWithClidcm);
						} catch (SecurityException e) {
							LOG.error(e.getMessage());
						}
						List<File> niftiGeneratedFiles = niftiFileSorting(existingFiles, directory, serieIDFolderFile);
						constructNiftiExpressionAndDatasetFiles(converter, dataset, serie, niftiGeneratedFiles);
						++index;

					}
				}
			} else if (serie.getDatasets().size() == 1) {
				// Need to construct nifti files for only one dataset in current serie
				Dataset dataset = serie.getDatasets().get(0);
				if (serieIDFolderFile.isDirectory()) {
					LOG.info("convertToNifti : create nifti files for the dataset {} in directory : {}", dataset.getName(), serieIDFolderFile.getName());
					final List<File> existingFiles = Arrays.asList(serieIDFolderFile.listFiles());
					NIfTIConverter converter = null;
					try {
						converter = datasetToNiftiConversionLauncher(dataset, serieIDFolderFile, serie, converterId, isConvertAs4D, isConvertWithClidcm);
					} catch (SecurityException e) {
						LOG.error(e.getMessage());
					}
					List<File> niftiGeneratedFiles = niftiFileSorting(existingFiles, serieIDFolderFile, serieIDFolderFile);
					constructNiftiExpressionAndDatasetFiles(converter, dataset, serie, niftiGeneratedFiles);
				}
			}
		}
	}

	/**
	 *  Build dataset Expresion and datasetFiles
	 * 
	 * @param converter
	 * @param dataset
	 * @param serie
	 * @param niftiGeneratedFiles
	 */
	private void constructNiftiExpressionAndDatasetFiles(NIfTIConverter converter, Dataset dataset, Serie serie, List<File> niftiGeneratedFiles) {
		// Build dataset Expresion and datasetFiles
		ExpressionFormat expressionFormat = generateNiftiDatasetExpression(dataset,serie);
		expressionFormat.setNiftiConverter(converter);
		dataset.getExpressionFormats().add(expressionFormat);
		generateNiftiDatasetFiles(expressionFormat, dataset, niftiGeneratedFiles);
	}


	/**
	 * This method extract the dicom files in proper dataset(s) (in a serie).
	 * It also constructs the associated ExpressionFormat and DatasetFiles within the Dataset object.
	 * 
	 * @param serieIDFolderFile
	 * @param serie
	 * @param serieIdentifiedForNotSeparating
	 */
	private void constructDicom(final File serieIDFolderFile, final Serie serie, final boolean serieIdentifiedForNotSeparating) {
		if (!serieIdentifiedForNotSeparating) {
			final HashMap<SerieToDatasetsSeparator, Dataset> datasetMap = new HashMap<>();
			for (Image image : serie.getImages()) {
				final int acquisitionNumber = image.getAcquisitionNumber();
				Set<EchoTime> echoTimes = image.getEchoTimes();
				double[] imageOrientationPatientsDoubleArray = convertDoubles(image.getImageOrientationPatient());
				SerieToDatasetsSeparator seriesToDatasetsSeparator =
						new SerieToDatasetsSeparator(acquisitionNumber, echoTimes, imageOrientationPatientsDoubleArray);
				boolean found = false;
				for (SerieToDatasetsSeparator seriesToDatasetsComparatorIterate : datasetMap.keySet()) {
					if (seriesToDatasetsComparatorIterate.equals(seriesToDatasetsSeparator)) {
						found = true;
						seriesToDatasetsSeparator = seriesToDatasetsComparatorIterate;
						break;
					}
				}
				// existing dataset has been found, just add the image/datasetFile
				if (found) {
					DatasetFile datasetFile = createDatasetFile(image);
					datasetMap.get(seriesToDatasetsSeparator).getExpressionFormats().get(0).getDatasetFiles().add(datasetFile);
					datasetMap.get(seriesToDatasetsSeparator).getFlipAngles().add(Double.valueOf(image.getFlipAngle()));
					datasetMap.get(seriesToDatasetsSeparator).getRepetitionTimes().add(image.getRepetitionTime());
					datasetMap.get(seriesToDatasetsSeparator).getInversionTimes().add(image.getInversionTime());
					datasetMap.get(seriesToDatasetsSeparator).setEchoTimes(image.getEchoTimes());
					// new dataset has to be created, new expression format and add image/datasetfile
				} else {
					Dataset dataset = new Dataset();
					ExpressionFormat expressionFormat = new ExpressionFormat();
					expressionFormat.setType("dcm");
					dataset.getExpressionFormats().add(expressionFormat);
					DatasetFile datasetFile = createDatasetFile(image);
					dataset.getFlipAngles().add(Double.valueOf(image.getFlipAngle()));
					dataset.getRepetitionTimes().add(image.getRepetitionTime());
					dataset.getInversionTimes().add(image.getInversionTime());
					dataset.setEchoTimes(image.getEchoTimes());
					expressionFormat.getDatasetFiles().add(datasetFile);
					datasetMap.put(seriesToDatasetsSeparator, dataset);
					serie.getDatasets().add(dataset);
				}
			}

			boolean success = true;
			// create a separate folder for each group of images
			int index = 0;
			for (final Entry<SerieToDatasetsSeparator, Dataset> datasets : datasetMap.entrySet()) {
				// create a folder
				final File folder = new File(serieIDFolderFile.getAbsolutePath() + File.separator + DATASET_STR + index);
				success = folder.mkdirs();
				if (!success) {
					LOG.error("deleteFolder : the creation of {} failed", folder);
				}
				// move the files into the folder
				for (final DatasetFile datasetFile : datasets.getValue().getExpressionFormats().get(0).getDatasetFiles()) {
					String path = datasetFile.getPath();
					final File oldFile = new File(path);
					if (oldFile.exists()) {
						final File newFile = new File(folder, oldFile.getName());
						success = oldFile.renameTo(newFile);
						datasetFile.setPath(newFile.getAbsolutePath());
						datasets.getValue().setName(serie.getSeriesDescription() + index);
						if (!success) {
							LOG.error("deleteFolder : moving of " + oldFile + " failed");
						}
					}
				}
				index++;
			}
			if (!success) {
				LOG.error("Error while constructing Dicom in constructDicom.");
			}
		} else {
			Dataset dataset = new Dataset();
			dataset.setName(serie.getSeriesDescription());
			ExpressionFormat expressionFormat = new ExpressionFormat();
			expressionFormat.setType("dcm");
			dataset.getExpressionFormats().add(expressionFormat);
			for (Image image : serie.getImages()) {
				dataset.getFlipAngles().add(Double.valueOf(image.getFlipAngle()));
				dataset.getRepetitionTimes().add(image.getRepetitionTime());
				dataset.getInversionTimes().add(image.getInversionTime());
				dataset.setEchoTimes(image.getEchoTimes());
				DatasetFile datasetFile = createDatasetFile(image);
				expressionFormat.getDatasetFiles().add(datasetFile);
			}
			serie.getDatasets().add(dataset);
		}
	}


	/**
	 * @param image
	 * @return
	 */
	private DatasetFile createDatasetFile(Image image) {
		DatasetFile datasetFile = new DatasetFile();
		datasetFile.setPath(image.getPath());
		datasetFile.setAcquisitionNumber(image.getAcquisitionNumber());
		datasetFile.setImageOrientationPatient(image.getImageOrientationPatient());
		return datasetFile;
	}

	/**
	 * This method creates a folder for each serie and moves into it the files,
	 * coming either from the PACS or from the zip upload directory.
	 * 
	 * @param seriesFolderFile
	 * @param serie
	 * @throws ShanoirException
	 */
	private File createSerieIDFolderAndMoveFiles(File workFolder, File seriesFolderFile, Serie serie) throws ShanoirException {
		String serieID = serie.getSeriesInstanceUID();
		File serieIDFolderFile = new File(seriesFolderFile.getAbsolutePath() + File.separator + serieID);
		if(!serieIDFolderFile.exists()) {
			serieIDFolderFile.mkdirs();
		} else {
			throw new ShanoirException("Error while creating serie id folder: folder already exists. serieId: " + serieID);
		}
		List<Image> images = serie.getImages();
		moveFiles(workFolder, serieIDFolderFile, images);
		return serieIDFolderFile;
	}

	/**
	 * This method moves the files into serie specific folders.
	 * 
	 * @param serieIDFolder
	 * @param images
	 * @throws RestServiceException
	 */
	private void moveFiles(File workFolder, File serieIDFolder, List<Image> images) throws ShanoirException {
		for (Iterator<Image> iterator = images.iterator(); iterator.hasNext();) {
			Image image = iterator.next();
			// the path has been set in processDicomFile in DicomFileAnalyzer before
			String filePath = image.getPath();
			File oldFile = new File(workFolder.getAbsolutePath() + File.separator + filePath);
			if (oldFile.exists()) {
				File newFile = new File(serieIDFolder.getAbsolutePath() + File.separator + oldFile.getName());
				oldFile.renameTo(newFile);
				LOG.debug("Moving file: {} to {}", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
				image.setPath(newFile.getAbsolutePath());
			} else {
				throw new ShanoirException("Error while creating serie id folder: file to copy does not exist.");
			}
		}
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}

	public static double[] convertDoubles(List<Double> doubles) {
		double[] ret = new double[doubles.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = doubles.get(i).doubleValue();
		}
		return ret;
	}

	/**
	 * Make a diff to know which files from destinationFolder are not in the
	 * given list of files.
	 *
	 * @param existingFiles
	 *            the existing files
	 * @param destinationFolder
	 *            the destination folder
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
	 * Check if the newly created nifti files list contains a .prop file
	 * If it is the case, then there has been a problem during conversion
	 * and should be considered as failed.
	 * 
	 */
	private boolean containsPropFile(List<File> niftiFiles){
		for(File current : niftiFiles){
			if(current.getPath().contains(".prop")) {
				return true;
			}
		}
		return false;
	}

}
