package com.doan.WEB_TMDT.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SocketIOConfig {

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:9092}")
    private Integer port;

    @Value("${socketio.bossCount:1}")
    private int bossCount;

    @Value("${socketio.workCount:100}")
    private int workCount;

    @Value("${socketio.allowCustomRequests:true}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout:10000}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout:60000}")
    private int pingTimeout;

    @Value("${socketio.pingInterval:25000}")
    private int pingInterval;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setBossThreads(bossCount);
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);

        // CORS configuration
        config.setOrigin("*");

        // Authentication
        config.setAuthorizationListener(new AuthorizationListener() {
            @Override
            public boolean isAuthorized(HandshakeData data) {
                // Lấy token từ query params hoặc header
                String token = data.getSingleUrlParam("token");

                if (token != null && !token.isEmpty()) {
                    // TODO: Validate JWT token
                    // return jwtTokenProvider.validateToken(token);
                    return true; // Tạm thời cho qua
                }

                log.warn("Unauthorized connection attempt");
                return false;
            }
        });

        return new SocketIOServer(config);
    }
}