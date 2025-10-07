package org.shanoir.ng.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.ActiveProfiles;

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

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class DatasetFileUtilsTest {

    @TempDir
    public File testFolder;

    @Test
    public void writeInputFileForExport() throws IOException {

        File sample = new File("src/test/resources/input.json");

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

        DatasetFileUtils.writeManifestForExport(out, files2AcquisitionId);

        out.close();

        ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(bytesOutputStream.toByteArray());
        ZipInputStream in = new ZipInputStream(bytesInputStream, StandardCharsets.UTF_8);
        ZipEntry readEntry = in.getNextEntry();
        assertNotNull(readEntry);
        assertNull(readEntry.getComment());
        assertEquals("input.json", readEntry.getName());
        in.close();
    }

}
