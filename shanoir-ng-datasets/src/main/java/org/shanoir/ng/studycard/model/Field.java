/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.studycard.model;

import org.shanoir.ng.dataset.modality.BidsDataType;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMetadata;
import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ExploredEntity;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.AcquisitionContrast;
import org.shanoir.ng.datasetacquisition.model.mr.ContrastAgentUsed;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolSCMetadata;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequenceApplication;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequencePhysics;

public enum Field implements DatasetFieldInterface {

	MODALITY_TYPE(1) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getDatasetModalityType() != null) {
					return dataset.getUpdatedMetadata().getDatasetModalityType().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			DatasetModalityType type = DatasetModalityType.valueOf(updatedValue);
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
				dataset.getUpdatedMetadata().setDatasetModalityType(type);
			}
		}
	},
	PROTOCOL_NAME(2) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrProtocol mrProtocol = mrDsAcq.getMrProtocol();
				if (mrProtocol != null) {
					if (mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
						return mrDsAcq.getMrProtocol().getUpdatedMetadata().getName();
					} else if (mrDsAcq.getMrProtocol().getOriginMetadata() != null) {
						return mrDsAcq.getMrProtocol().getOriginMetadata().getName();
					}
				}
			}
			return null;
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setName(updatedValue);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	PROTOCOL_COMMENT(3) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getComment();
				}
			}
			return null;
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setComment(updatedValue);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	TRANSMITTING_COIL(4) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getTransmittingCoilId().toString();
				}
			}
			return null;
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setTransmittingCoilId(Long.parseLong(updatedValue));
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
		
	},
	RECEIVING_COIL(5) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getReceivingCoilId().toString();
				}
			}
			return null;
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setReceivingCoilId(Long.parseLong(updatedValue));
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	EXPLORED_ENTITY(6) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
					return dataset.getUpdatedMetadata().getExploredEntity().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			ExploredEntity exploredEntity = ExploredEntity.valueOf(updatedValue);
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
				dataset.getUpdatedMetadata().setExploredEntity(exploredEntity);			
			}
		}
	},
	ACQUISITION_CONTRAST(7) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getAcquisitionContrast().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				AcquisitionContrast contrast = AcquisitionContrast.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setAcquisitionContrast(contrast);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	MR_SEQUENCE_APPLICATION(8) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceApplication().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrSequenceApplication mrSequenceApplication = MrSequenceApplication.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequenceApplication(mrSequenceApplication);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	MR_SEQUENCE_PHYSICS(9) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequencePhysics().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrSequencePhysics mrSequencePhysics = MrSequencePhysics.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequencePhysics(mrSequencePhysics);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	NAME(10) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
					return dataset.getUpdatedMetadata().getName();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
				dataset.getUpdatedMetadata().setName(updatedValue);			
			}
		}
	},
	COMMENT(11) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
					return dataset.getUpdatedMetadata().getComment();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
				dataset.getUpdatedMetadata().setComment(updatedValue);			
			}
		}
	},
	MR_SEQUENCE_NAME(12) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceName();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequenceName(updatedValue);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	CONTRAST_AGENT_USED(13) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getContrastAgentUsed().name();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				ContrastAgentUsed contrastAgentUsed = ContrastAgentUsed.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setContrastAgentUsed(contrastAgentUsed);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	},
	MR_DATASET_NATURE(14) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset instanceof MrDataset) {
					MrDataset mrDataset = (MrDataset) dataset;
					if (mrDataset.getUpdatedMrMetadata() == null && mrDataset.getUpdatedMrMetadata() != null) {
						mrDataset.getUpdatedMrMetadata().getMrDatasetNature().name();	
					}		
				} else {
					throw new IllegalArgumentException("dataset should be of type MrDataset");
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			MrDatasetNature nature = MrDatasetNature.valueOf(updatedValue);
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (dataset instanceof MrDataset) {
					MrDataset mrDataset = (MrDataset) dataset;
					if (mrDataset.getUpdatedMrMetadata() == null) mrDataset.setUpdatedMrMetadata(new MrDatasetMetadata());
					mrDataset.getUpdatedMrMetadata().setMrDatasetNature(nature);			
				} else {
					throw new IllegalArgumentException("dataset should be of type MrDataset");
				}
			}
		}
	},
	BIDS_DATA_TYPE(15) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getBidsDataType();
				}
			}
			return null;
		}
		
		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) {
			BidsDataType dataType = BidsDataType.valueOf(updatedValue);
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setBidsDataType(dataType);
			} else {
				throw new IllegalArgumentException("datasetAcquisition should be of type MrDatasetAcquisition");
			}
		}
	};
	
	private int id;
	
	private Field(int id) {
		this.id = id;
	}
	
	public static Field getEnum(int id) {
		for (Field field : Field.values()) {
			if (field.getId() == id) return field;
		}
		return null;
	}
	
	public int getId() {
		return id;
	}

}
