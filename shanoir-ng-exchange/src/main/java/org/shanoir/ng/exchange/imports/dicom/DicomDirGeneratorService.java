package org.shanoir.ng.exchange.imports.dicom;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.tool.common.FilesetInfo;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean generates a DICOMDIR file.
 * 
 * @author mkain
 *
 */
//@Service
public class DicomDirGeneratorService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomDirGeneratorService.class);

	private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

	private FilesetInfo fsInfo = new FilesetInfo();

	private boolean checkDuplicate = false;

	private RecordFactory recFact = new RecordFactory();

	private DicomDirReader in;

	private DicomDirWriter out;

	public void generateDicomDirFromDirectory(File dicomDir, File directory) throws IOException {
		createDicomDir(dicomDir);
		addReferenceTo(directory, dicomDir);
		close();
	}

	private void createDicomDir(File file) throws IOException {
		DicomDirWriter.createEmptyDirectory(file, UIDUtils.createUIDIfNull(fsInfo.getFilesetUID()),
				fsInfo.getFilesetID(), fsInfo.getDescriptorFile(), fsInfo.getDescriptorFileCharset());
		in = out = DicomDirWriter.open(file);
		out.setEncodingOptions(encOpts);
	}

	private int addReferenceTo(File f, File dicomDir) throws IOException {
		int n = 0;
		if (f.isDirectory()) {
			for (String s : f.list()) {
				n += addReferenceTo(new File(f, s), dicomDir);
			}
			return n;
		}
		// do not add reference to DICOMDIR
		if (f.equals(dicomDir)) {
			return 0;
		}

		Attributes fmi;
		Attributes dataset;
		DicomInputStream din = null;
		try {
			din = new DicomInputStream(f);
			din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
			fmi = din.readFileMetaInformation();
			dataset = din.readDataset(-1, Tag.PixelData);
		} catch (IOException e) {
			LOG.error("failed to parse image '" + f + "' - " + e.getMessage());
			return 0;
		} finally {
			if (din != null) {
				try {
					din.close();
				} catch (Exception ignore) {
				}
			}
		}
		char prompt = '.';
		if (fmi == null) {
			fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
			prompt = 'F';
		}
		String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
		if (iuid == null) {
			LOG.error("skip DICOM file '" + f + "' without SOP Instance UID (0008, 0018)");
			return 0;
		}
		return addRecords(dataset, n, out.toFileIDs(f), prompt, iuid, fmi);
	}

	private int addRecords(Attributes dataset, int num, String[] fileIDs, char prompt, String iuid, Attributes fmi)
			throws IOException {
		String pid = dataset.getString(Tag.PatientID, null);
		String styuid = dataset.getString(Tag.StudyInstanceUID, null);
		String seruid = dataset.getString(Tag.SeriesInstanceUID, null);

		if (styuid != null) {
			if (pid == null) {
				dataset.setString(Tag.PatientID, VR.LO, pid = styuid);
				prompt = prompt == 'F' ? 'P' : 'p';
			}
			Attributes patRec = in.findPatientRecord(pid);
			if (patRec == null) {
				patRec = recFact.createRecord(RecordType.PATIENT, null, dataset, null, null);
				out.addRootDirectoryRecord(patRec);
				num++;
			}
			Attributes studyRec = in.findStudyRecord(patRec, styuid);
			if (studyRec == null) {
				studyRec = recFact.createRecord(RecordType.STUDY, null, dataset, null, null);
				out.addLowerDirectoryRecord(patRec, studyRec);
				num++;
			}

			if (seruid != null) {
				Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
				if (seriesRec == null) {
					seriesRec = recFact.createRecord(RecordType.SERIES, null, dataset, null, null);
					out.addLowerDirectoryRecord(studyRec, seriesRec);
					num++;
				}

				if (iuid != null) {
					Attributes instRec;
					if (checkDuplicate) {
						instRec = in.findLowerInstanceRecord(seriesRec, false, iuid);
						if (instRec != null) {
							System.out.print('-');
							return 0;
						}
					}
					instRec = recFact.createRecord(dataset, fmi, fileIDs);
					out.addLowerDirectoryRecord(seriesRec, instRec);
					num++;
				}
			}
		} else {
			if (iuid != null) {
				if (checkDuplicate) {
					if (in.findRootInstanceRecord(false, iuid) != null) {
						System.out.print('-');
						return 0;
					}
				}
				Attributes instRec = recFact.createRecord(dataset, fmi, fileIDs);
				out.addRootDirectoryRecord(instRec);
				prompt = prompt == 'F' ? 'R' : 'r';
				num++;
			}
		}
		return num;
	}

	protected void close() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException ignore) {
			}
		}
		in = null;
		out = null;
	}

}
