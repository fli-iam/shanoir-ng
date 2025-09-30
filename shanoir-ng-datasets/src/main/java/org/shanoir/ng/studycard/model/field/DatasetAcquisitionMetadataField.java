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

package org.shanoir.ng.studycard.model.field;


import com.fasterxml.jackson.annotation.JsonTypeName;
import org.shanoir.ng.dataset.modality.BidsDataType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.*;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;

@JsonTypeName("DatasetAcquisitionMetadataField")
public enum DatasetAcquisitionMetadataField implements MetadataFieldInterface<DatasetAcquisition> {

	PROTOCOL_NAME(2) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrProtocol mrProtocol = mrDsAcq.getMrProtocol();
				if (mrProtocol != null) {
					if (mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getName() != null) {
						return mrDsAcq.getMrProtocol().getUpdatedMetadata().getName();
					} else if (mrDsAcq.getMrProtocol().getOriginMetadata() != null) {
						return mrDsAcq.getMrProtocol().getOriginMetadata().getName();
					}
				}
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setName(updatedValue);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	PROTOCOL_COMMENT(3) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getComment();
				}
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setComment(updatedValue);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	TRANSMITTING_COIL(4) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getTransmittingCoilId() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getTransmittingCoilId().toString();
				} else if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getOriginMetadata() != null && mrDsAcq.getMrProtocol().getOriginMetadata().getTransmittingCoilId() != null) {
                    return mrDsAcq.getMrProtocol().getOriginMetadata().getTransmittingCoilId().toString();
                }
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
			    Long longUpdatedValue = Long.getLong(updatedValue);
			    if (longUpdatedValue != null) {
			        MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
			        if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
			        mrDsAcq.getMrProtocol().getUpdatedMetadata().setTransmittingCoilId(longUpdatedValue);
			    }
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

	},
	RECEIVING_COIL(5) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getReceivingCoilId() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getReceivingCoilId().toString();
				} else if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getOriginMetadata().getReceivingCoilId() != null) {
                    return mrDsAcq.getMrProtocol().getOriginMetadata().getReceivingCoilId().toString();
                }
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
			    Long longUpdatedValue = Long.getLong(updatedValue);
                if (longUpdatedValue != null) {
                    MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
                    if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
                    mrDsAcq.getMrProtocol().getUpdatedMetadata().setReceivingCoilId(longUpdatedValue);
                }
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	ACQUISITION_CONTRAST(7) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getAcquisitionContrast() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getAcquisitionContrast().name();
				} else if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getOriginMetadata() != null && mrDsAcq.getMrProtocol().getOriginMetadata().getAcquisitionContrast() != null) {
                    return mrDsAcq.getMrProtocol().getOriginMetadata().getAcquisitionContrast().name();
                }
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				AcquisitionContrast contrast = AcquisitionContrast.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setAcquisitionContrast(contrast);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	MR_SEQUENCE_APPLICATION(8) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceApplication() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceApplication().name();
				}
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrSequenceApplication mrSequenceApplication = MrSequenceApplication.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequenceApplication(mrSequenceApplication);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	MR_SEQUENCE_PHYSICS(9) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequencePhysics().name();
				}
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				MrSequencePhysics mrSequencePhysics = MrSequencePhysics.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequencePhysics(mrSequencePhysics);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	MR_SEQUENCE_NAME(12) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceName() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getMrSequenceName();
				} else if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getOriginMetadata() != null) {
                    return mrDsAcq.getMrProtocol().getOriginMetadata().getMrSequenceName();
                }
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setMrSequenceName(updatedValue);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	CONTRAST_AGENT_USED(13) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata().getContrastAgentUsed() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getContrastAgentUsed().name();
				} else if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getOriginMetadata() != null && mrDsAcq.getMrProtocol().getOriginMetadata().getContrastAgentUsed() != null) {
                    return mrDsAcq.getMrProtocol().getOriginMetadata().getContrastAgentUsed().name();
                }
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				ContrastAgentUsed contrastAgentUsed = ContrastAgentUsed.valueOf(updatedValue);
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setContrastAgentUsed(contrastAgentUsed);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	},
	BIDS_DATA_TYPE(15) {
		@Override
		public String get(DatasetAcquisition datasetAcquisition) throws CheckedIllegalClassException {
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol() != null && mrDsAcq.getMrProtocol().getUpdatedMetadata() != null) {
					return mrDsAcq.getMrProtocol().getUpdatedMetadata().getBidsDataType();
				}
				return null;
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}

		@Override
		public void update(DatasetAcquisition datasetAcquisition, String updatedValue) throws CheckedIllegalClassException {
			BidsDataType dataType = BidsDataType.valueOf(updatedValue);
			if (datasetAcquisition instanceof MrDatasetAcquisition) {
				MrDatasetAcquisition mrDsAcq = (MrDatasetAcquisition) datasetAcquisition;
				if (mrDsAcq.getMrProtocol().getUpdatedMetadata() == null) mrDsAcq.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
				mrDsAcq.getMrProtocol().getUpdatedMetadata().setBidsDataType(dataType);
			} else {
				throw new CheckedIllegalClassException(MrDatasetAcquisition.class, datasetAcquisition);
			}
		}
	};

	private int id;

	private DatasetAcquisitionMetadataField(int id) {
		this.id = id;
	}

	public static DatasetAcquisitionMetadataField getEnum(int id) {
		for (DatasetAcquisitionMetadataField field : DatasetAcquisitionMetadataField.values()) {
			if (field.getId() == id) return field;
		}
		return null;
	}

	@Override
	public int getId() {
		return id;
	}

	public static boolean has(int id) {
        return getEnum(id) != null;
    }

	public static boolean has(String name) {
        return getEnum(name) != null;
    }

    public static DatasetAcquisitionMetadataField getEnum(String name) {
        for (DatasetAcquisitionMetadataField f : DatasetAcquisitionMetadataField.values()) {
            if (f.name().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return null;
    }
}
