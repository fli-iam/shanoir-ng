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

package org.shanoir.ng.shared.configuration;

import org.shanoir.ng.shared.email.EmailBase;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailStudy;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
import org.shanoir.ng.shared.email.StudyInvitationEmail;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@RegisterReflectionForBinding({
        EmailBase.class,
        EmailDatasetsImported.class,
        EmailDatasetImportFailed.class,
        EmailStudy.class,
        EmailStudyUsersAdded.class,
        StudyInvitationEmail.class,
        RabbitTemplate.class,
        SimpleMessageConverter.class,
        ContentTypeDelegatingMessageConverter.class,
        JacksonJsonMessageConverter.class
})
@ImportRuntimeHints(NativeImageHintsConfiguration.RabbitMQHints.class)
public class NativeImageHintsConfiguration {

    static class RabbitMQHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                    RabbitTemplate.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS);
            hints.reflection().registerType(
                    SimpleRabbitListenerContainerFactory.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS);
            hints.reflection().registerType(
                    ContentTypeDelegatingMessageConverter.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
            hints.reflection().registerType(
                    JacksonJsonMessageConverter.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
            hints.reflection().registerType(
                    SimpleMessageListenerContainer.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
            hints.reflection().registerType(
                    ConnectionFactory.class,
                    MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }

}
