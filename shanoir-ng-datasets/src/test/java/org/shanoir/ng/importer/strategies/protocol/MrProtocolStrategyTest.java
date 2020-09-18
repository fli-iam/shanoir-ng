package org.shanoir.ng.importer.strategies.protocol;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the implementation of MrProtocolStrategy.
 * 
 * @author mkain
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MrProtocolStrategyTest {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrProtocolStrategy.class);
	
	@InjectMocks
	private MrProtocolStrategy mrProtocolStrategy;
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testGenerateMrProtocolForSerieNotEnhancedMR() throws IOException {
		Attributes attributes = getAttributesFromFile("/1.3.12.2.1107.5.2.43.166066.2018042412210060639615964");
		Serie serie = generateSerie(attributes);
		MrProtocol mrProtocol = mrProtocolStrategy.generateProtocolForSerie(attributes, serie);
		Assert.assertTrue(mrProtocol.getNumberOfAverages().equals(1));
		Assert.assertTrue(mrProtocol.getFilters().equals("77"));
	}	

//	@Test
//	public void testGenerateMrProtocolForSerieEnhancedMR() throws IOException {
//		Attributes attributes = getAttributesFromFile("/DICOM_IM_0022");
//		Serie serie = generateSerie(attributes);
//		MrProtocol mrProtocol = mrProtocolStrategy.generateMrProtocolForSerie(attributes, serie);
////		logMrProtocol(mrProtocol);
//	}

	private void logMrProtocol(MrProtocol mrProtocol) {
		Field[] fieldArrayMrProtocol = mrProtocol.getClass().getDeclaredFields();
		Field[] fieldArrayMrProtocolMetadata = mrProtocol.getOriginMetadata().getClass().getDeclaredFields();

		SortedSet<Field> fields = new TreeSet<Field>(new FieldComparator());
		fields.addAll(Arrays.asList(concat(fieldArrayMrProtocol, fieldArrayMrProtocolMetadata)));

		StringBuffer b = new StringBuffer("All About ");
		b.append(mrProtocol.getClass().getName());
		b.append("\nFields:\n");
		for (Field field : fields) {
			field.setAccessible(true);
			b.append(field.getName());
			b.append(";");
			Object value = null;
			try {
				value = field.get(mrProtocol);
			} catch (IllegalArgumentException e) {
				try {
					value = field.get(mrProtocol.getOriginMetadata());
				} catch (IllegalArgumentException | IllegalAccessException e1) {
				}
			} catch (IllegalAccessException e) {
			}
			if (value != null && !field.getName().contains("Coil")) {
				b.append(value.toString());
			} else {
				b.append("null");
			}
			b.append("\n");
		}
		LOG.warn(b.toString());
	}

	private static Field[] concat(Field[] first, Field[] second) {
		List<Field> both = new ArrayList<Field>(first.length + second.length);
		Collections.addAll(both, first);
		Collections.addAll(both, second);
		return both.toArray(new Field[both.size()]);
	}

	private static class FieldComparator implements Comparator<Field> {
		@Override
		public int compare(Field f1, Field f2) {
			return f1.getName().compareTo(f2.getName());
		}
	}
	
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
