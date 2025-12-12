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

package org.shanoir.ng.preclinical.subjects.service;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.dto.SubjectDto;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.repository.AnimalSubjectRepository;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AnimalSubjects service implementation.
 *
 * @author sloury
 *
 */
@Service
public class AnimalSubjectServiceImpl implements AnimalSubjectService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnimalSubjectServiceImpl.class);

    @Autowired
    private AnimalSubjectRepository subjectsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void deleteById(final Long id) {
        subjectsRepository.deleteById(id);
    }

    @Override
    public List<AnimalSubject> findAll() {
        return Utils.toList(subjectsRepository.findAll());
    }

    @Override
    public AnimalSubject getById(final Long id) {
        return subjectsRepository.findById(id).orElse(null);
    }

    @Override
    public AnimalSubject save(final AnimalSubject subject) throws ShanoirException {
        AnimalSubject savedSubject;
        try {
            savedSubject = subjectsRepository.save(subject);
        } catch (DataIntegrityViolationException dive) {
            LOG.error("Error while creating  AnimalSubject:  ", dive);
            throw new ShanoirException("Error while creating  AnimalSubject:  ", dive);
        }
        return savedSubject;
    }

    @Override
    public AnimalSubject update(final AnimalSubject subject) throws ShanoirException {
        final AnimalSubject subjectDB = subjectsRepository.findById(subject.getId()).orElse(null);
        updateSubjectValues(subjectDB, subject);
        try {
            subjectsRepository.save(subjectDB);
        } catch (Exception e) {
            LOG.error("Error while updating  AnimalSubject:  ", e);
            throw new ShanoirException("Error while updating  AnimalSubject:  ", e);
        }
        return subjectDB;
    }

    private AnimalSubject updateSubjectValues(final AnimalSubject subjectDb, final AnimalSubject subject) {
        subjectDb.setId(subject.getId());
        subjectDb.setBiotype(subject.getBiotype());
        subjectDb.setProvider(subject.getProvider());
        subjectDb.setSpecie(subject.getSpecie());
        subjectDb.setStabulation(subject.getStabulation());
        subjectDb.setStrain(subject.getStrain());
        updateSubjectPathologiesValues(subjectDb.getSubjectPathologies(), subject.getSubjectPathologies());
        updateSubjectTherapiesValues(subjectDb.getSubjectTherapies(), subject.getSubjectTherapies());
        return subjectDb;
    }

    private void updateSubjectPathologiesValues(List<SubjectPathology> existingSubjectPathologies, final List<SubjectPathology> newSubjectPathologies) {
        Utils.syncList(existingSubjectPathologies, newSubjectPathologies, (target, src) -> {
            target.setLocation(src.getLocation());
            target.setPathology(src.getPathology());
            target.setPathologyModel(src.getPathologyModel());
            target.setStartDate(src.getStartDate());
            target.setEndDate(src.getEndDate());
        });
    }

    private void updateSubjectTherapiesValues(List<SubjectTherapy> existingSubjectTherapies, final List<SubjectTherapy> newSubjectTherapies) {
        Utils.syncList(existingSubjectTherapies, newSubjectTherapies, (target, src) -> {
            if (src.getTherapy() != null) target.setTherapy(src.getTherapy());
            target.setDose(src.getDose());
            target.setStartDate(src.getStartDate());
            target.setEndDate(src.getEndDate());
            target.setDoseUnit(src.getDoseUnit());
            target.setFrequency(src.getFrequency());
            target.setMolecule(src.getMolecule());
        });
    }

    @Override
    public List<AnimalSubject> findByReference(Reference reference) {
        return Utils.toList(subjectsRepository.findByReference(reference));
    }

    @Override
    public boolean isSubjectNameAlreadyUsedInStudy(String name, Long studyId) {
        return (boolean) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_NAME_QUEUE, name);
    }

    @Override
    public Long createSubject(SubjectDto dto) throws JsonProcessingException, ShanoirException {
        Long subjectId;
        subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, mapper.writeValueAsString(dto));
        if (subjectId == null) {
            throw new ShanoirException("Created subject id is null.");
        }
        return subjectId;
    }

    @Override
    public List<AnimalSubject> findByIds(List<Long> ids) {
        return Utils.toList(subjectsRepository.findAllById(ids));
    }

}
