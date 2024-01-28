package com.chat.websocketstomp.dao;

import com.chat.websocketstomp.domain.ChatRoom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRoomRepository {

    private Map<String, ChatRoom> chatRooms;

    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    public void save(String roomId, ChatRoom room) {
        chatRooms.put(roomId, room);
    }

    public List<ChatRoom> findAllRoom() {
        List<ChatRoom> findRooms = new ArrayList<>(chatRooms.values());
        Collections.reverse(findRooms);
        return findRooms;
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }

}
