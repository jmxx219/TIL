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
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatService chatService;

    @GetMapping("/room")
    public String rooms(Model model) {
        return "/chat/room";
    }

    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        return chatService.createChatRoom(name);
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> getRooms() {
        return chatService.findAllRoom();
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatService.findRoomById(roomId);
    }
}
