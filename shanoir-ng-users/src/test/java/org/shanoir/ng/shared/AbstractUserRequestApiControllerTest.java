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

package org.shanoir.ng.shared.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AbstractUserRequestApiControllerTest {

    /** Minimal concrete subclass that makes generateUsername accessible. */
    static class TestableController extends AbstractUserRequestApiController {
        public void generateUsernamePublic(User user) {
            generateUsername(user);
        }
    }

    @Mock
    private UserRepository userRepository;

    private TestableController controller;

    @BeforeEach
    void setUp() {
        controller = new TestableController();
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User userWithName(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private void assertUsername(String firstName, String lastName, String expected) {
        User user = userWithName(firstName, lastName);
        controller.generateUsernamePublic(user);
        assertEquals(expected, user.getUsername());
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void simpleName() {
        assertUsername("Jean", "Dupont", "jdupont");
    }

    @Test
    void hyphenatedFirstName() {
        assertUsername("Jean-Pierre", "Dupont", "jpdupont");
    }

    @Test
    void hyphenatedLastName() {
        assertUsername("Jean", "Dupont-Martin", "jdupontmartin");
    }

    @Test
    void compoundFirstNameWithSpace() {
        assertUsername("Jean Pierre", "Dupont", "jpdupont");
    }

    @Test
    void compoundLastNameWithSpace() {
        assertUsername("Jean", "Dupont Martin", "jdupontmartin");
    }

    @Test
    void unicodeAccentedChars() {
        // accents stripped by stripAccents at the end
        assertUsername("André", "Müller", "amuller");
    }

    @Test
    void apostropheInLastName() {
        // apostrophe is allowed by the sanitization regex and kept in the username
        assertUsername("Jean", "O'Brien", "jo'brien");
    }

    @Test
    void digitsStrippedFromFirstName() {
        assertUsername("Jean123", "Smith", "jsmith");
    }

    @Test
    void specialCharsStrippedFromLastName() {
        assertUsername("Jean", "Smith@!", "jsmith");
    }

    @Test
    void specialCharsStrippedFromBothNames() {
        assertUsername("J@ohn", "Smith%", "jsmith");
    }

    @Test
    void leadingHyphenInFirstName() {
        // "-Jean" splits to ["", "Jean"]; empty guard prevents crash and skips ""
        assertUsername("-Jean", "Dupont", "jdupont");
    }

    @Test
    void doubleHyphenInFirstName() {
        // "Jean--Pierre" splits to ["Jean", "", "Pierre"]; empty guard skips ""
        assertUsername("Jean--Pierre", "Dupont", "jpdupont");
    }

    @Test
    void leadingHyphenInLastName() {
        assertUsername("Jean", "-Dupont", "jdupont");
    }

    @Test
    void doubleHyphenInLastName() {
        assertUsername("Jean", "Dupont--Martin", "jdupontmartin");
    }

    @Test
    void allSpecialCharsInFirstName() {
        // all stripped → no initial appended; only last name contributes
        assertUsername("@#$%", "Smith", "smith");
    }

    @Test
    void usernameCollisionAppendsCounter() {
        when(userRepository.findByUsername("jdupont")).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername("jdupont1")).thenReturn(Optional.empty());

        assertUsername("Jean", "Dupont", "jdupont1");
    }

    @Test
    void multipleUsernameCollisionsIncrementsCounter() {
        when(userRepository.findByUsername("jdupont")).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername("jdupont1")).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername("jdupont2")).thenReturn(Optional.empty());

        assertUsername("Jean", "Dupont", "jdupont2");
    }

    // ── unicode / script extremes ─────────────────────────────────────────────

    @Test
    void frenchAccentOnFirstLetterOfFirstName() {
        // É → toLowerCase é → stripAccents e
        assertUsername("Élodie", "Martin", "emartin");
    }

    @Test
    void frenchAccentOnFirstLetterOfLastName() {
        // Étoile → toLowerCase étoile → stripAccents etoile
        assertUsername("Jean", "Étoile", "jetoile");
    }

    @Test
    void nordicRingAboveAndUmlaut() {
        // Å → a (a + combining ring), ö → o (o + combining diaeresis)
        assertUsername("Åsa", "Lindström", "alindstrom");
    }

    @Test
    void germanUmlautInBothNames() {
        // Ü → u, Öztürk → ozturk
        assertUsername("Ümit", "Öztürk", "uozturk");
    }

    @Test
    void greekAlphabet() {
        // Greek \p{L} chars pass the sanitization regex; tonos stripped via NFD
        // Ν → ν (no accent), Παπαδόπουλος → παπαδοπουλος
        assertUsername("Νίκος", "Παπαδόπουλος", "νπαπαδοπουλος");
    }

    @Test
    void cyrillicAlphabet() {
        // Basic Cyrillic letters pass \p{L} and have no NFD-removable combining chars
        assertUsername("Иван", "Петров", "ипетров");
    }
}
