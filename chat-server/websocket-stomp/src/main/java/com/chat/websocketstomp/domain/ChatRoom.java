package com.chat.websocketstomp.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRoom {
    private String roomId;
    private String name;

    @Builder
    public ChatRoom(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }
}
