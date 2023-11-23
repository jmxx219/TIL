package com.chat.websocket.handler;

import com.chat.websocket.ChatService;
import com.chat.websocket.domain.ChatMessage;
import com.chat.websocket.domain.ChatRoom;
import com.chat.websocket.domain.MessageType;
import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final Gson gson = new Gson();
    private final ChatService chatService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 웹소켓 연결
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[afterConnectionEstablished] " + session.getId());
        var sessionId = session.getId();
        sessions.put(sessionId, session); // 세션에 저장

//        ChatMessage message = ChatMessage.builder().sender(sessionId).build();
//        message.newConnect();
//
//        sessions.values().forEach(s -> { // 모든 세션에 알림
//            try {
//                if(!s.getId().equals(sessionId)) {
//                    s.sendMessage(new TextMessage(message.toString()));
//                }
//            }
//            catch (Exception e) {
//                // TODO: throw
//            }
//        });
    }

    /**
     * 양방향 데이터 통신
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        ChatMessage chatMessage = gson.fromJson(textMessage.getPayload(), ChatMessage.class);
        ChatRoom chatRoom = chatService.findRoomById(chatMessage.getRoomId());

        log.info("chatMessage {}", chatMessage.toString());

        if(chatRoom != null) {
            if (chatMessage.getType().equals(MessageType.ENTER)) {
                chatRoom.addSession(session);
                chatMessage.setEnterMessage();
            }
            chatRoom.getSessions().parallelStream().forEach(s -> {
                if(!s.getId().equals(session.getId())) {
                    chatService.sendMessage(s, chatMessage);
                }
            });
        }
    }

    /**
     * 소켓 연결 종료
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var sessionId = session.getId();

        sessions.remove(sessionId);

        final ChatMessage message = new ChatMessage();
        message.closeConnect();
        message.setSender(sessionId);

        sessions.values().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(message.toString()));
            }
            catch (Exception e) {
                // TODO: throw
            }
        });
    }

    /**
     * 소켓 통신 에러
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }
}
