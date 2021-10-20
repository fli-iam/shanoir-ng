package org.shanoir.ng.preclinical.anesthetics.ingredients;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class AnestheticIngredientUniqueValidator extends UniqueConstraintManagerImpl<AnestheticIngredient> {

}
