package org.shanoir.ng.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


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
