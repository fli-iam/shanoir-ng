package org.shanoir.ng.importer.strategies.protocol;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.importer.dto.Serie;

/**
 * Tests the implementation of MrProtocolStrategy.
 * 
 * @author mkain
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MrProtocolStrategyTest {

	@InjectMocks
	private MrProtocolStrategy mrProtocolStrategy;
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testGenerateMrProtocolForSerieNotEnhancedMR() throws IOException {
		Attributes attributes = getAttributesFromFile("/1.3.12.2.1107.5.2.43.166066.2018042412210060639615964");
		Serie serie = generateSerie(attributes);
		MrProtocol mrProtocol = mrProtocolStrategy.generateMrProtocolForSerie(attributes, serie);
		Assert.assertTrue(mrProtocol.getNumberOfAverages().equals(1));
		Assert.assertTrue(mrProtocol.getFilters().equals("77"));
	}	

//	@Test
//	public void testGenerateMrProtocolForSerieEnhancedMR() throws IOException {
//		Attributes attributes = getAttributesFromFile("/IM_0029");
//		Serie serie = generateSerie(attributes);
//		MrProtocol mrProtocol = mrProtocolStrategy.generateMrProtocolForSerie(attributes, serie);
//	}

	private Attributes getAttributesFromFile(String fileNameInClassPath) throws IOException {
		File dicomFile = new File(this.getClass().getResource(fileNameInClassPath).getFile());
		DicomInputStream dIS = new DicomInputStream(dicomFile);
		// we read all file here with pixel data, as same method for dicom enhanced too and there required
		Attributes attributes = dIS.readDataset(-1, -1);
		dIS.close();
		return attributes;
	}

	private Serie generateSerie(Attributes attributes) {
		Serie serie = new Serie();
		if (UID.EnhancedMRImageStorage.equals(attributes.getString(Tag.SOPClassUID))) {
			serie.setIsMultiFrame(true);
			serie.setIsEnhancedMR(true);
			serie.setMultiFrameCount(getFrameCount(attributes));
			serie.setSequenceName(attributes.getString(Tag.PulseSequenceName));
		} else {
			serie.setIsMultiFrame(false);
			serie.setIsEnhancedMR(false);
			serie.setSequenceName(attributes.getString(Tag.SequenceName));
		}
		serie.setProtocolName(attributes.getString(Tag.ProtocolName));
		return serie;
	}
	
	private int getFrameCount(final Attributes attributes) {
		if (attributes != null) {
			Attributes pffgs = attributes.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence);
			if (pffgs != null) {
				return pffgs.size();
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

}
