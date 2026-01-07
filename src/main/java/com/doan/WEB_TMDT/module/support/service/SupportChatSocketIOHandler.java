package com.doan.WEB_TMDT.module.support.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.doan.WEB_TMDT.module.support.dto.*;
import com.doan.WEB_TMDT.module.support.dto.response.SupportReplyResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
// xét sự kiện
public class SupportChatSocketIOHandler {

    private final SocketIOServer server;
    private final SupportReplyService replyService;

    private final ConcurrentMap<Long, ConcurrentHashMap<String, SocketIOClient>> ticketRooms =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, UserStatusDTO> clientUserInfo =
            new ConcurrentHashMap<>();

    @PostConstruct
    public void startServer() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("join_ticket", JoinRoomDTO.class, onJoinTicket());
        server.addEventListener("send_message", ChatMessageDTO.class, onSendMessage());
        server.addEventListener("typing", TypingIndicatorDTO.class, onTyping());
        server.addEventListener("stop_typing", TypingIndicatorDTO.class, onStopTyping());
        server.start();
        log.info("Socket.IO server started on port {}", server.getConfiguration().getPort());
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket.IO server stopped");
    }

    private ConnectListener onConnected() {
        return client -> {
            String sessionId = client.getSessionId().toString();
            String email = client.getHandshakeData().getSingleUrlParam("email");
            String userName = client.getHandshakeData().getSingleUrlParam("userName");

            if (email != null && userName != null) {
                clientUserInfo.put(sessionId, UserStatusDTO.builder()
                        .userName(userName)
                        .userType("unknown")
                        .status("online")
                        .build());
            }

            log.info("Client connected: {} | user: {}", sessionId, userName);
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            String sessionId = client.getSessionId().toString();
            UserStatusDTO user = clientUserInfo.remove(sessionId);
            if (user != null) {
                broadcastToRoom(user.getTicketId(), "user_left", user);
            }
            log.info("Client disconnected: {}", sessionId);
        };
    }

    private DataListener<JoinRoomDTO> onJoinTicket() {
        return (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();

            // Tạo room nếu chưa có
            ticketRooms(data.getTicketId());

            // Lưu client vào room
            ticketRooms.get(data.getTicketId()).put(sessionId, client);

            // Lưu user info kèm email
            String email = client.getHandshakeData().getSingleUrlParam("email");
            clientUserInfo.put(sessionId, UserStatusDTO.builder()
                    .ticketId(data.getTicketId())
                    .email(email)
                    .userName(data.getUserName())
                    .userType(data.getUserType())
                    .status("online")
                    .build());

            client.sendEvent("joined_ticket", Map.of("success", true, "ticketId", data.getTicketId()));
            broadcastToRoom(data.getTicketId(), "user_joined", data.getUserName());
        };
    }


    private DataListener<ChatMessageDTO> onSendMessage() {
        return (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            UserStatusDTO user = clientUserInfo.get(sessionId);
            if (user == null) {
                sendError(client, "User not authenticated");
                return;
            }

            SupportReplyResponse saved = replyService.saveMessageFromWebSocket(
                    data.getTicketId(), data.getContent(), user.getUserName());

            ChatMessageDTO msg = ChatMessageDTO.builder()
                    .ticketId(data.getTicketId())
                    .messageId(saved.getId())
                    .content(saved.getContent())
                    .senderType(saved.getSenderType())
                    .senderName(saved.getSenderName())
                    .senderEmail(user.getEmail())
                    .timestamp(saved.getCreatedAt().toString())
                    .build();

            broadcastToRoom(data.getTicketId(), "new_message", msg);
        };
    }

    private DataListener<TypingIndicatorDTO> onTyping() {
        return (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            UserStatusDTO user = clientUserInfo.get(sessionId);
            if (user != null) {
                broadcastToRoom(data.getTicketId(), "user_typing",
                        new TypingIndicatorDTO(data.getTicketId(), user.getUserName(), true),
                        sessionId);
            }
        };
    }

    private DataListener<TypingIndicatorDTO> onStopTyping() {
        return (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            UserStatusDTO user = clientUserInfo.get(sessionId);
            if (user != null) {
                broadcastToRoom(data.getTicketId(), "user_stop_typing",
                        new TypingIndicatorDTO(data.getTicketId(), user.getUserName(), false),
                        sessionId);
            }
        };
    }

    public ConcurrentHashMap<String, SocketIOClient> ticketRooms(Long ticketId) {
        return ticketRooms.computeIfAbsent(ticketId, k -> new ConcurrentHashMap<>());
    }

    private void broadcastToRoom(Long ticketId, String event, Object data) {
        ConcurrentHashMap<String, SocketIOClient> clients = ticketRooms.get(ticketId);
        if (clients != null) {
            clients.values().forEach(c -> c.sendEvent(event, data));
        }
    }

    private void broadcastToRoom(Long ticketId, String event, Object data, String exceptId) {
        ConcurrentHashMap<String, SocketIOClient> clients = ticketRooms.get(ticketId);
        if (clients != null) {
            clients.forEach((id, c) -> {
                if (!id.equals(exceptId)) c.sendEvent(event, data);
            });
        }
    }

    private void sendError(SocketIOClient client, String message) {
        client.sendEvent("error", new ErrorMessageDTO(message, "ERROR"));
    }
}
