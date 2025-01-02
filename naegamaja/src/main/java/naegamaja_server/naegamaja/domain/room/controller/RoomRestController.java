package naegamaja_server.naegamaja.domain.room.controller;

import lombok.RequiredArgsConstructor;
import naegamaja_server.naegamaja.domain.room.dto.RoomCreationDto;
import naegamaja_server.naegamaja.domain.room.dto.RoomRequest;
import naegamaja_server.naegamaja.domain.room.dto.RoomResponse;
import naegamaja_server.naegamaja.domain.room.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomRestController {

    private final RoomService roomService;

    @PostMapping("/{roomId}/join")
    public void joinRoom(@RequestHeader String authorization, @RequestBody RoomRequest.JoinRoomRequest request) {
        roomService.joinRoom(request, authorization);
    }

    @PostMapping("/create")
    public int createRoom(@RequestHeader String authorization, @RequestBody RoomRequest.CreateRoomRequest request) {
        System.out.println("request = " + request);
        return roomService.createRoom(request, authorization);
    }



}
