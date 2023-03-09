package org.shanoir.ng.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
public class DatasetFileUtilsTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void writeInputFileForExport() throws IOException {

        File sample = new File("src/test/resources/input.json");
        assertTrue("Sample file " + sample.getAbsolutePath() + " does not exists !", sample.exists());

        File tmpDir = testFolder.getRoot();
        tmpDir.createNewFile();

        Map<Long, List<File>> files2AcquisitionId = new HashMap<>();

        File[] files1 = { new File("/path/to/file_1.dcm"),
                new File("/path/to/file_2.dcm"),
                new File("/path/to/file_3.dcm") };

        File[] files2 = { new File("/path/to/file_4.dcm"),
                new File("/path/to/file_5.dcm") };

        files2AcquisitionId.put(1L, Arrays.asList(files1));
        files2AcquisitionId.put(2L, Arrays.asList(files2));

        DatasetFileUtils.writeInputFileForExport(tmpDir, files2AcquisitionId);

        File input = new File(tmpDir.getAbsolutePath() + File.separator + DatasetFileUtils.INPUT);

        assertTrue(input.getAbsolutePath() + " does not exists !", input.exists());
        assertTrue("Content of " + input.getAbsolutePath() + " differs from content of test/resources/input.json sample file !", FileUtils.contentEquals(input, sample));



    }

}
