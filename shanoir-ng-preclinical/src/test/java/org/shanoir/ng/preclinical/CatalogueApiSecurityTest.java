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

package org.shanoir.ng.preclinical;

import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticApiController;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticEditableByManager;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticUniqueValidator;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientApiController;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientEditableByManager;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientService;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientUniqueValidator;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyApiController;
import org.shanoir.ng.preclinical.pathologies.PathologyEditableByManager;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.pathologies.PathologyUniqueValidator;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModelApiController;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModelEditableByManager;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModelService;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModelUniqueValidator;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.ReferenceUniqueValidator;
import org.shanoir.ng.preclinical.references.RefsApiController;
import org.shanoir.ng.preclinical.references.RefsEditableByManager;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyApiController;
import org.shanoir.ng.preclinical.therapies.TherapyEditableByManager;
import org.shanoir.ng.preclinical.therapies.TherapyService;
import org.shanoir.ng.preclinical.therapies.TherapyUniqueValidator;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.assertion.AccessCheckedFunction0Arg;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Rights on the reference catalogue of ms preclinical (references, pathologies, pathology models, therapies,
 * anesthetics and their ingredients), shared by all studies like the centers: every user can view it,
 * only experts and admins can change it.
 *
 * The services are mocked: only the access decision is checked, not what the endpoints do.
 */
@SpringBootTest
@ActiveProfiles("test")
public class CatalogueApiSecurityTest {

    private static final long USER_ID = 2L;
    private static final long ID = 1L;
    private static final long OTHER_ID = 2L;

    @Autowired
    private RefsApiController refs;

    @Autowired
    private PathologyApiController pathologies;

    @Autowired
    private PathologyModelApiController models;

    @Autowired
    private TherapyApiController therapies;

    @Autowired
    private AnestheticApiController anesthetics;

    @Autowired
    private AnestheticIngredientApiController ingredients;

    @MockitoBean
    private RefsService refsService;

    @MockitoBean
    private ReferenceUniqueValidator referenceUniqueValidator;

    @MockitoBean
    private RefsEditableByManager refsEditableByManager;

    @MockitoBean
    private PathologyService pathologyService;

    @MockitoBean
    private PathologyUniqueValidator pathologyUniqueValidator;

    @MockitoBean
    private PathologyEditableByManager pathologyEditableByManager;

    @MockitoBean
    private PathologyModelService pathologyModelService;

    @MockitoBean
    private PathologyModelUniqueValidator pathologyModelUniqueValidator;

    @MockitoBean
    private PathologyModelEditableByManager pathologyModelEditableByManager;

    @MockitoBean
    private TherapyService therapyService;

    @MockitoBean
    private TherapyUniqueValidator therapyUniqueValidator;

    @MockitoBean
    private TherapyEditableByManager therapyEditableByManager;

    @MockitoBean
    private AnestheticService anestheticService;

    @MockitoBean
    private AnestheticUniqueValidator anestheticUniqueValidator;

    @MockitoBean
    private AnestheticEditableByManager anestheticEditableByManager;

    @MockitoBean
    private AnestheticIngredientService anestheticIngredientService;

    @MockitoBean
    private AnestheticIngredientUniqueValidator anestheticIngredientUniqueValidator;

    @MockitoBean
    private AnestheticIngredientEditableByManager anestheticIngredientEditableByManager;

    @MockitoBean
    private ShanoirEventService eventService;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        checkReads(false);
        checkWrites(false);
        checkAllFiles(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUser() throws ShanoirException {
        checkReads(true);
        checkWrites(false);
        checkAllFiles(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpert() throws ShanoirException {
        checkReads(true);
        checkWrites(true);
        checkAllFiles(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        checkReads(true);
        checkWrites(true);
        checkAllFiles(true);
    }

    private void checkReads(boolean authorized) throws ShanoirException {
        check(authorized, () -> refs.getReferencesByCategory("subject"));
        check(authorized, () -> refs.getReferencesByCategoryAndType("subject", "specie"));
        check(authorized, () -> refs.getReferenceCategories());
        check(authorized, () -> refs.getReferenceTypesByCategory("subject"));
        check(authorized, () -> refs.getReferences());
        check(authorized, () -> refs.getReferenceById(ID));
        check(authorized, () -> refs.getReferenceByCategoryTypeAndValue("subject", "specie", "rat"));

        check(authorized, () -> pathologies.getPathologyById(ID));
        check(authorized, () -> pathologies.getPathologies());

        check(authorized, () -> models.getPathologyModelById(ID));
        check(authorized, () -> models.getPathologyModels());
        check(authorized, () -> models.getPathologyModelsByPathology(ID));
        check(authorized, () -> models.downloadModelSpecifications(ID));

        check(authorized, () -> therapies.getTherapyById(ID));
        check(authorized, () -> therapies.getTherapyByType("drug"));
        check(authorized, () -> therapies.getTherapies());

        check(authorized, () -> anesthetics.getAnestheticById(ID));
        check(authorized, () -> anesthetics.getAnesthetics());
        check(authorized, () -> anesthetics.getAnestheticsByType("injection"));

        check(authorized, () -> ingredients.getAnestheticIngredientById(ID, OTHER_ID));
        check(authorized, () -> ingredients.getAnestheticIngredients(ID));
    }

    private void checkWrites(boolean authorized) throws ShanoirException {
        Reference reference = new Reference();
        check(authorized, () -> refs.createReferenceValue(reference));
        check(authorized, () -> refs.updateReferenceValue(ID, reference, bindingResult(reference)));
        check(authorized, () -> refs.deleteReferenceValue(ID));

        Pathology pathology = new Pathology();
        check(authorized, () -> pathologies.createPathology(pathology, bindingResult(pathology)));
        check(authorized, () -> pathologies.updatePathology(ID, pathology, bindingResult(pathology)));
        check(authorized, () -> pathologies.deletePathology(ID));

        PathologyModel model = new PathologyModel();
        check(authorized, () -> models.createPathologyModel(model, bindingResult(model)));
        check(authorized, () -> models.updatePathologyModel(ID, model, bindingResult(model)));
        check(authorized, () -> models.uploadModelSpecifications(ID, new MultipartFile[0]));
        check(authorized, () -> models.deletePathologyModel(ID));

        Therapy therapy = new Therapy();
        check(authorized, () -> therapies.createTherapy(therapy, bindingResult(therapy)));
        check(authorized, () -> therapies.updateTherapy(ID, therapy, bindingResult(therapy)));
        check(authorized, () -> therapies.deleteTherapy(ID));

        Anesthetic anesthetic = new Anesthetic();
        check(authorized, () -> anesthetics.createAnesthetic(anesthetic, bindingResult(anesthetic)));
        check(authorized, () -> anesthetics.updateAnesthetic(ID, anesthetic, bindingResult(anesthetic)));
        check(authorized, () -> anesthetics.deleteAnesthetic(ID));

        AnestheticIngredient ingredient = new AnestheticIngredient();
        check(authorized, () -> ingredients.createAnestheticIngredient(ID, ingredient, bindingResult(ingredient)));
        check(authorized, () -> ingredients.updateAnestheticIngredient(ID, OTHER_ID, ingredient, bindingResult(ingredient)));
        check(authorized, () -> ingredients.deleteAnestheticIngredient(ID, OTHER_ID));
    }

    /** The list of all the stored files stays reserved to admins. */
    private void checkAllFiles(boolean authorized) throws ShanoirException {
        check(authorized, () -> models.getAllFiles());
    }

    private static void check(boolean authorized, AccessCheckedFunction0Arg call) throws ShanoirException {
        if (authorized) {
            assertAccessAuthorized(call);
        } else {
            assertAccessDenied(call);
        }
    }

    private static BeanPropertyBindingResult bindingResult(Object target) {
        return new BeanPropertyBindingResult(target, "target");
    }

}
