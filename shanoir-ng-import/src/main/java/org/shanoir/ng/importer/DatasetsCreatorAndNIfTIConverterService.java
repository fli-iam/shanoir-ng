package org.shanoir.ng.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.shanoir.ng.importer.model.Dataset;
import org.shanoir.ng.importer.model.DatasetFile;
import org.shanoir.ng.importer.model.DiffusionGradient;
import org.shanoir.ng.importer.model.EchoTime;
import org.shanoir.ng.importer.model.ExpressionFormat;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.DiffusionUtil;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.ShanoirExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
	private ShanoirExec shanoirExec;

	@Autowired
	private ShanoirEventService shanoirEventService;
	
	@Value("${shanoir.import.series.seriesProperties}")
	private String seriesProperties;

	@Value("${shanoir.conversion.converters.convertwithclidcm}")
	private String convertWithClidcm;
	
	@Value("${shanoir.conversion.converters.path}")
	private String convertersPath;

	/** Logs of the conversion. */
	private String conversionLogs;

	/** Output files mapped by series UID. */
	private HashMap<String, List<String>> outputFiles = new HashMap<>();

	Random rand = new Random();

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public void createDatasetsAndRunConversion(Patient patient, File workFolder, ImportJob importJob) throws ShanoirException {
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
			List<Serie> series = study.getSelectedSeries();
			float progress = 0;

			int nbSeries = series.size();
			int cpt = 1;

			for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
				Serie serie = seriesIt.next();

				progress = progress + (0.5f / series.size());
				importJob.getShanoirEvent().setProgress(progress);
				importJob.getShanoirEvent().setMessage("Converting to NIfTI for serie [" + (serie.getProtocolName() == null ? serie.getSeriesInstanceUID() : serie.getProtocolName()) + "] (" + cpt + "/" + nbSeries + ")...");
				shanoirEventService.publishEvent(importJob.getShanoirEvent());

				File serieIDFolderFile = createSerieIDFolderAndMoveFiles(workFolder, seriesFolderFile, serie);
				boolean serieIdentifiedForNotSeparating;
				try {
					serieIdentifiedForNotSeparating = checkSerieForPropertiesString(serie, seriesProperties);
					// if the serie is not one of the series, that should not be separated, please separate the series,
					// otherwise just do not separate the series and keep all images for one nii conversion
					serie.setDatasets(new ArrayList<Dataset>());
					constructDicom(serieIDFolderFile, serie, serieIdentifiedForNotSeparating);
				} catch (NoSuchFieldException | SecurityException e) {
					LOG.error(e.getMessage());
				}
				// as images/non-images are migrated to datasets, clear the list now
				serie.getImages().clear();
				serie.getNonImages().clear();
				cpt++;
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
				double[] imageOrientationPatientsDoubleArray = image.getImageOrientationPatient() == null ? null : image.getImageOrientationPatient().stream().mapToDouble(i->i).toArray();
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
				File newFile = new File(serieIDFolder.getAbsolutePath() + File.separator + filePath);
				newFile.getParentFile().mkdirs();
				boolean success = oldFile.renameTo(newFile);
				if (!success) {
					throw new ShanoirException("Error while creating serie id folder: file to copy already exists.");
				}
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