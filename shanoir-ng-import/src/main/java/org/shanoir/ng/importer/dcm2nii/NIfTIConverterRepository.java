package org.shanoir.ng.importer.dcm2nii;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface NIfTIConverterRepository extends CrudRepository<NIfTIConverter, Long> {
	
	/**
	 * Get all nifti converters
	 * 
	 * @return list of nifti converters.
	 */
	List<NIfTIConverter> findAll();

}