package com.chat.websocketstomp.controller;

import com.chat.websocketstomp.domain.ChatRoom;
import com.chat.websocketstomp.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat/room")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String roomName) {
        return chatService.createChatRoom(roomName);
    }

    @GetMapping
    @ResponseBody
    public List<ChatRoom> getRooms() {
        return chatService.findAllRoom();
    }

    @GetMapping("/{roomId}")
    @ResponseBody
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatService.findRoomById(roomId);
    }
}
