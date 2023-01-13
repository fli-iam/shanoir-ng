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

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMetadata;
import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ExploredEntity;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DatasetMetadataField")
public enum DatasetMetadataField implements MetadataFieldInterface<Dataset> {

	MODALITY_TYPE(1) {
		@Override
		public String get(Dataset dataset) {
			if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getDatasetModalityType() != null) {
				return dataset.getUpdatedMetadata().getDatasetModalityType().name();
			} 
			else return null;
		}
		
		@Override
		public void update(Dataset dataset, String updatedValue) {
			DatasetModalityType type = DatasetModalityType.valueOf(updatedValue);
			if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
			dataset.getUpdatedMetadata().setDatasetModalityType(type);
		}
	},
	EXPLORED_ENTITY(6) {
		@Override
		public String get(Dataset dataset) {
			if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
				return dataset.getUpdatedMetadata().getExploredEntity().name();
			}
			return null;
		}
		
		@Override
		public void update(Dataset dataset, String updatedValue) {
			ExploredEntity exploredEntity = ExploredEntity.valueOf(updatedValue);
			if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
			dataset.getUpdatedMetadata().setExploredEntity(exploredEntity);			
		}
	},
	NAME(10) {
		@Override
		public String get(Dataset dataset) {
			if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
				return dataset.getUpdatedMetadata().getName();
			}
			return null;
		}
		
		@Override
		public void update(Dataset dataset, String updatedValue) {
			if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
			dataset.getUpdatedMetadata().setName(updatedValue);			
		}
	},
	COMMENT(11) {
		@Override
		public String get(Dataset dataset) {
			if (dataset.getUpdatedMetadata() == null && dataset.getUpdatedMetadata().getExploredEntity() != null) {
				return dataset.getUpdatedMetadata().getComment();
			}
			return null;
		}
		
		@Override
		public void update(Dataset dataset, String updatedValue) {
			if (dataset.getUpdatedMetadata() == null) dataset.setUpdatedMetadata(new DatasetMetadata());
			dataset.getUpdatedMetadata().setComment(updatedValue);			
		}
	},
	MR_DATASET_NATURE(14) {
		@Override
		public String get(Dataset dataset) {
			if (dataset instanceof MrDataset) {
				MrDataset mrDataset = (MrDataset) dataset;
				if (mrDataset.getUpdatedMrMetadata() == null && mrDataset.getUpdatedMrMetadata() != null) {
					mrDataset.getUpdatedMrMetadata().getMrDatasetNature().name();	
				}		
				return null;
			} else {
				throw new IllegalArgumentException("dataset should be of type MrDataset");
			}
		}
		
		@Override
		public void update(Dataset dataset, String updatedValue) {
			MrDatasetNature nature = MrDatasetNature.valueOf(updatedValue);
			if (dataset instanceof MrDataset) {
				MrDataset mrDataset = (MrDataset) dataset;
				if (mrDataset.getUpdatedMrMetadata() == null) mrDataset.setUpdatedMrMetadata(new MrDatasetMetadata());
				mrDataset.getUpdatedMrMetadata().setMrDatasetNature(nature);			
			} else {
				throw new IllegalArgumentException("dataset should be of type MrDataset");
			}
		}
	};
	
	private int id;
	
	private DatasetMetadataField(int id) {
		this.id = id;
	}
	
	public static DatasetMetadataField getEnum(int id) {
		for (DatasetMetadataField field : DatasetMetadataField.values()) {
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

    public static DatasetMetadataField getEnum(String name) {
        for (DatasetMetadataField f : DatasetMetadataField.values()) {
            if (f.name().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return null;
    }

}
