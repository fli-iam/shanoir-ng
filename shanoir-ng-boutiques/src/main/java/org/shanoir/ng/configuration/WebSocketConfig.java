package org.shanoir.ng.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
//import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

   @Override
   public void configureMessageBroker(MessageBrokerRegistry config) {
       config.enableSimpleBroker("/message");
       config.setApplicationDestinationPrefixes("/boutiques");
   }
   
   @Override
   public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:4200").withSockJS();
       registry.addEndpoint("/ws").setAllowedOrigins("https://shanoir-ng-nginx").withSockJS();
   }

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
		// TODO Auto-generated method stub
		return false;
	}
   

//	@Override
//	public void configureClientInboundChannel(ChannelRegistration registration) {
//		registration.interceptors(new ChannelInterceptor() {
//			@Override
//			public Message<?> preSend(Message<?> message, MessageChannel channel) {
//				StompHeaderAccessor accessor =
//						MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//
//					// Authentication user = ... ; // access authentication header(s)
//					
//					Optional.ofNullable(accessor.getNativeHeader("Authorization")).ifPresent(ah -> {
//			            String bearerToken = ah.get(0).replace("Bearer ", "");
//			            log.debug("Received bearer token {}", bearerToken);
//			            JWSAuthenticationToken token = (JWSAuthenticationToken) authenticationManager
//			                .authenticate(new JWSAuthenticationToken(bearerToken));
//			            
//					accessor.setUser(user);
//				}
//				return message;
//			}
//		});
//	}

}
