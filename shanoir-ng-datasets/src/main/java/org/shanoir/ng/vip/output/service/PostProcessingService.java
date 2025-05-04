package org.shanoir.ng.vip.output.service;

import org.shanoir.ng.vip.output.exception.ResultHandlerException;

import java.util.List;

public interface PostProcessingService {
    /**
     * Post process the given processing ids
     *
     * @param processingIds list of processing ids to post process
     * @param comment
     */
    void launchPostProcessing(List<Long> processingIds, String comment) throws ResultHandlerException;
}
