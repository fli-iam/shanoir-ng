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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.rabbitmq.client.Channel;

/**
 * Checks that the custom listener container factories apply the
 * spring.rabbitmq.listener.simple.* properties and never requeue a rejected message.
 * No broker is needed: the connection factory is lazy.
 */
public class RabbitMQConfigurationTest {

    private static final String[] FACTORIES = {"multipleConsumersFactory", "singleConsumerFactory"};

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RabbitAutoConfiguration.class))
            .withUserConfiguration(RabbitMQConfiguration.class);

    private final ApplicationContextRunner retryContextRunner = contextRunner.withPropertyValues(
            "spring.rabbitmq.listener.simple.retry.enabled=true",
            "spring.rabbitmq.listener.simple.retry.max-retries=2",
            "spring.rabbitmq.listener.simple.retry.initial-interval=1ms");

    @Test
    public void rejectedMessagesAreNotRequeued() {
        contextRunner.run(context -> {
            for (String name : FACTORIES) {
                SimpleRabbitListenerContainerFactory factory = context.getBean(name, SimpleRabbitListenerContainerFactory.class);
                assertThat(ReflectionTestUtils.getField(factory, "defaultRequeueRejected")).isEqualTo(false);
            }
        });
    }

    @Test
    public void noRetryWhenRetryIsDisabled() {
        contextRunner.run(context -> {
            for (String name : FACTORIES) {
                SimpleRabbitListenerContainerFactory factory = context.getBean(name, SimpleRabbitListenerContainerFactory.class);
                assertThat(factory.getAdviceChain()).isNullOrEmpty();
            }
        });
    }

    @Test
    public void failingMessageIsRetriedUpToMaxRetries() {
        retryContextRunner.run(context -> {
            for (String name : FACTORIES) {
                ProxyMethodInvocation invocation = failingInvocation(new IllegalStateException("transient"));
                assertThrows(ListenerExecutionFailedException.class, () -> retryInterceptor(context.getBean(name, SimpleRabbitListenerContainerFactory.class)).invoke(invocation));
                // first attempt + 2 retries
                verify(invocation, times(3)).proceed();
            }
        });
    }

    @Test
    public void amqpRejectAndDontRequeueExceptionIsNotRetried() {
        retryContextRunner.run(context -> {
            for (String name : FACTORIES) {
                ProxyMethodInvocation invocation = failingInvocation(new AmqpRejectAndDontRequeueException("final"));
                assertThrows(ListenerExecutionFailedException.class, () -> retryInterceptor(context.getBean(name, SimpleRabbitListenerContainerFactory.class)).invoke(invocation));
                verify(invocation, times(1)).proceed();
            }
        });
    }

    private MethodInterceptor retryInterceptor(SimpleRabbitListenerContainerFactory factory) {
        assertThat(factory.getAdviceChain()).hasSize(1);
        return (MethodInterceptor) factory.getAdviceChain()[0];
    }

    /**
     * Invocation of the listener as the container does it (channel, message), failing with the
     * given cause wrapped in a ListenerExecutionFailedException.
     */
    private ProxyMethodInvocation failingInvocation(Throwable cause) throws Throwable {
        Message message = new Message("payload".getBytes(), new MessageProperties());
        ProxyMethodInvocation invocation = mock(ProxyMethodInvocation.class);
        when(invocation.getArguments()).thenReturn(new Object[] {mock(Channel.class), message});
        when(invocation.invocableClone()).thenReturn(invocation);
        when(invocation.getMethod()).thenReturn(Object.class.getMethod("toString"));
        when(invocation.proceed()).thenThrow(new ListenerExecutionFailedException("listener failed", cause, message));
        return invocation;
    }

}
