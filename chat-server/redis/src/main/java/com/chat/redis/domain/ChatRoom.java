package com.chat.redis.domain;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;

    @Builder
    public ChatRoom(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }
}
