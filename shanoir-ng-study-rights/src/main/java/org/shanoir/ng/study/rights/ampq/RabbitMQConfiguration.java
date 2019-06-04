//package org.shanoir.ng.study.rights.ampq;
//
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * RabbitMQ configuration.
// */
//@Configuration
//public class RabbitMQConfiguration {
//
//	@Bean
//	public static Queue studyUserQueue() {
//		return new Queue("study-user-queue-import", true);
//	}
//	
//	@Bean
//    public FanoutExchange fanout() {
//        return new FanoutExchange("study-user-exchange", true, false);
//    }
//
//}
