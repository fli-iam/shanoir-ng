package org.shanoir.ng.manufacturermodel.service;

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class ManufacturerUniqueConstraintManager extends UniqueConstraintManagerImpl<Manufacturer> implements UniqueConstraintManager<Manufacturer> {

}
