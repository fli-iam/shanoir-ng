package org.shanoir.ng.dataset.service;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class DatasetTransactionalServiceImpl {

    //Service used for avoiding self-transaction issue

    private static final Logger LOG = LoggerFactory.getLogger(DatasetTransactionalServiceImpl.class);

    @Autowired
    @Lazy
    private DatasetServiceImpl datasetService;

    @Autowired
    private DatasetRepository repository;

    @Autowired
    private DatasetExpressionRepository deRepository;

    @Transactional
    protected Future<Void> deletePartitionOfNiftis(List<Long> partition, float total, ShanoirEvent event) {

        float progress = event.getProgress();
        for (Dataset dataset : repository.findAllById(partition)) {
            progress += 1f / total;
            datasetService.updateEvent(progress, event);
            deleteNifti(dataset);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Deletes nifti on file server
     * @param dataset
     */
    public void deleteNifti(Dataset dataset) {
        Set<DatasetExpression> expressionsToDelete = new HashSet<>();

        for (Iterator<DatasetExpression> iterex = dataset.getDatasetExpressions().iterator(); iterex.hasNext(); ) {
            DatasetExpression expression = iterex.next();
            if (!DatasetExpressionFormat.NIFTI_SINGLE_FILE.equals(expression.getDatasetExpressionFormat())) {
                continue;
            }
            for (Iterator<DatasetFile> iter = expression.getDatasetFiles().iterator(); iter.hasNext(); ) {
                DatasetFile file = iter.next();
                URL url = null;
                try {
                    url = new URL(file.getPath().replaceAll("%20", " "));
                    File srcFile = new File(UriUtils.decode(url.getPath(), StandardCharsets.UTF_8.name()));
                    if (srcFile.exists()) {
                        LOG.error("Deleting: " + srcFile.getAbsolutePath());
                        FileUtils.delete(srcFile);
                    }
                    // We are forced to detach elements here to be able to delete them from DB
                    file.setDatasetExpression(null);
                    iter.remove();
                } catch (Exception e) {
                    LOG.error("Could not delete nifti file: {}", file.getPath(), e);
                }
            }
            expression.setDataset(null);
            iterex.remove();
            expressionsToDelete.add(expression);
        }
        if (expressionsToDelete.isEmpty()) {
            return;
        }
        deRepository.deleteAll(expressionsToDelete);
    }
}
