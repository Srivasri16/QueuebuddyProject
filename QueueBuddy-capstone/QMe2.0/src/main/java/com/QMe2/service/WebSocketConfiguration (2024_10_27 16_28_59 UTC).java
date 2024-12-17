package com.QMe2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.QMe2.controller.WebSocketH;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
	@Autowired
	WebSocketH webS;

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		
	    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
	    container.setAsyncSendTimeout((long) 3600000);
	    container.setMaxSessionIdleTimeout((long) 3600000);
	    container.setMaxBinaryMessageBufferSize(1024000);
	    return container;
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		
	    registry.addHandler(webS, "/socket").setAllowedOrigins("*");
	}
}
