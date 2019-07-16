package com.example.docker_paas_2.config;

import com.example.docker_paas_2.websocket.ContainerExecHandshakeInterceptor;
import com.example.docker_paas_2.websocket.ContainerExecWSHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;


//加载websocket.
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

//    @Bean
//    public ServerEndpointExporter serverEndpointExporter(ApplicationContext context) {
//        return new ServerEndpointExporter();
//    }

    @Resource
    public ContainerExecWSHandler containerExecWSHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(containerExecWSHandler, "/ws/container/exec")
                .addInterceptors(new ContainerExecHandshakeInterceptor())
                .setAllowedOrigins("*");
    }






}