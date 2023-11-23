package com.chat.websocketstomp.service;

import com.chat.websocketstomp.dao.ChatRoomRepository;
import com.chat.websocketstomp.domain.ChatMessage;
import com.chat.websocketstomp.domain.ChatRoom;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
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
    private final ChatRoomRepository chatRoomRepository;

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

    public ChatRoom createChatRoom(String roomName) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .name(roomName)
                .build();
        chatRoomRepository.save(roomId, chatRoom);
        return chatRoom;
    }

    public List<ChatRoom> findAllRoom() {
        return chatRoomRepository.findAllRoom();
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }


}
