package com.chat.websocket;

import com.chat.websocket.domain.ChatMessage;
import com.chat.websocket.domain.ChatRoom;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ObjectMapper objectMapper;
    private Map<String, ChatRoom> chatRooms;

    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    public ChatRoom createRoom(String roomName) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .name(roomName)
                .build();
        chatRooms.put(roomId, chatRoom);
        return chatRoom;
    }

    public <T> void sendMessage(WebSocketSession receiver, ChatMessage message) {
        try {
            if(receiver != null && receiver.isOpen()) { // 타켓이 존재하고, 연결된 상태라면 메세지 전송
                receiver.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<ChatRoom> findAllRoom() {
        return (List<ChatRoom>) chatRooms.values();
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }
}
