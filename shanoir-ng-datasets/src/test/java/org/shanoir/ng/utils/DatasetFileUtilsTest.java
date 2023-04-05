package org.shanoir.ng.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class DatasetFileUtilsTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void writeInputFileForExport() throws IOException {
      
        File sample = new File("src/test/resources/input.json");
        assertTrue("Sample file " + sample.getAbsolutePath() + " does not exists !", sample.exists());

        Map<Long, List<String>> files2AcquisitionId = new HashMap<>();

        String[] files1 = { "/path/to/file_1.dcm",
                "/path/to/file_2.dcm",
                "/path/to/file_3.dcm" };

        String[] files2 = { "/path/to/file_4.dcm",
               "/path/to/file_5.dcm" };

        files2AcquisitionId.put(1L, Arrays.asList(files1));
        files2AcquisitionId.put(2L, Arrays.asList(files2));
       
        ByteArrayOutputStream bytesOutputStream = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(bytesOutputStream, StandardCharsets.UTF_8);

        DatasetFileUtils.writeInputFileForExport(out, files2AcquisitionId);

        out.close();

        ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(bytesOutputStream.toByteArray());
        ZipInputStream in = new ZipInputStream(bytesInputStream, StandardCharsets.UTF_8);
        ZipEntry readEntry = in.getNextEntry();
        assertNull("ZipInputStream must not retrieve comments", readEntry.getComment());
        assertNotNull(readEntry);
        assertEquals("input stream does not exists !", "input.json", readEntry.getName());
        in.close();
    }

}
