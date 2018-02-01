package org.shanoir.ng.importer.strategies.protocol;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.importer.dto.Serie;

public interface ProtocolStrategy {

	MrProtocol generateMrProtocolForSerie(Attributes dicomAttributes, Serie serie);

}