
package naegamaja_server.naegamaja.system.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naegamaja_server.naegamaja.domain.auth.service.RedisAuthService;
import naegamaja_server.naegamaja.domain.game.Service.RedisStreamGameMessagePublisher;
import naegamaja_server.naegamaja.domain.room.dto.RoomUserInfo;
import naegamaja_server.naegamaja.domain.room.repository.CustomRedisRoomRepository;
import naegamaja_server.naegamaja.domain.room.service.RedisPubSubRoomInfoPublisher;
import naegamaja_server.naegamaja.domain.session.repository.CustomRedisSessionRepository;
import naegamaja_server.naegamaja.system.websocket.dto.MeogajoaMessage;
import naegamaja_server.naegamaja.system.websocket.manager.WebSocketConnectionManager;
import naegamaja_server.naegamaja.system.websocket.model.MessageType;
import naegamaja_server.naegamaja.system.websocket.model.StompPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInboundChannelInterceptor implements ChannelInterceptor {

    private final RedisAuthService redisAuthService;
    private final WebSocketConnectionManager webSocketConnectionManager;
    private final CustomRedisSessionRepository customRedisSessionRepository;
    private final CustomRedisRoomRepository customRedisRoomRepository;
    private final RedisPubSubRoomInfoPublisher redisPubSubRoomInfoPublisher;
    private final RedisStreamGameMessagePublisher redisStreamGameMessagePublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getFirstNativeHeader("Authorization");

        StompCommand command = accessor.getCommand();

        if(command != null) {
            System.out.println("command: " + command + "로 들어왔다");
            System.out.println("sessionId: " + sessionId);
        }

        if(command == StompCommand.DISCONNECT) {
            return message;
        }

        if(command == null) return message;

        if(!redisAuthService.isValidSessionId(sessionId)) {
        }else{
        }

        switch (command) {
            case SUBSCRIBE:
                handleSubscribe(accessor, sessionId);
                break;
            case UNSUBSCRIBE:
                handleUnsubscribe(accessor, sessionId);
                break;
            case DISCONNECT:
                handleDisconnect(accessor, sessionId);
                break;
            case CONNECT:
                handleConnect(accessor, sessionId);
                break;
            case SEND:
                break;
        }

        return message;
    }

    private void handleSubscribe(StompHeaderAccessor accessor, String sessionId) {
        //System.out.println("최종구독");

        String destination = accessor.getDestination();
        System.out.println("destination: " + destination);

        String[] parts = destination.split("/");
        for(int i = 0; i < parts.length; i++){
            System.out.println("parts[" + i + "]: " + parts[i]);
        }

        if(parts.length >= 4){
            String type = parts[2];
            String id = parts[3];

            log.info("{}의 {}에 구독하였습니다.", sessionId, destination);

            processSubscription(type, id, sessionId);
        }

        if(parts.length == 5) {
            String type = parts[2];
            String getTwo = parts[3];
            String want = parts[4];

            if (type.equals("game") && want.equals("chat")) {
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GET_GAME_CHAT)
                        .gameId(getTwo)
                        .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }

            if (type.equals("user")) {
                if (want.equals("gameInfo")) {
                    //테스트할 때 sub 시 sessionId가 안 담겨서 잠시 쓰는 용도
                    String tempSessionId = customRedisSessionRepository.getSessionIdByNickname(getTwo);
                    String roomId = customRedisSessionRepository.getRoomIdBySessionId(tempSessionId);
                    if (roomId.equals("-1")) {
                        System.out.println("여기서 걸렸음 1월 27일");
                        //throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
                    }

                    MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                            .type(MessageType.GAME_MY_INFO)
                            .gameId(roomId)
                            .sender(getTwo)
                            .content("")
                            .build();
                    redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
                }

                if(want.equals("gameChat")){
                    String tempSessionId = customRedisSessionRepository.getSessionIdByNickname(getTwo);
                    String roomId = customRedisSessionRepository.getRoomIdBySessionId(tempSessionId);
                    if (roomId.equals("-1")) {
                        System.out.println("여기서 걸렸음 1월 27일");
                        //throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
                    }

                    MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                            .type(MessageType.GET_GAME_CHAT_PERSONAL)
                            .gameId(roomId)
                            .sender(getTwo)
                            .content("")
                            .build();

                    redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
                }
            }
        }

        if(parts.length == 6){
            String type = parts[2];
            String id = parts[3];
            String get4 = parts[4];
            String get5 = parts[5];

            if(type.equals("game") && get4.equals("notice") && get5.equals("system")){
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GAME_DAY_OR_NIGHT)
                        .gameId(id)
                        .sender("")
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }else if(type.equals("game") && get4.equals("chat") && get5.equals("black")){
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GET_GAME_CHAT_BLACK)
                        .gameId(id)
                        .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }else if(type.equals("game") && get4.equals("chat") && get5.equals("white")) {
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GET_GAME_CHAT_WHITE)
                        .gameId(id)
                        .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }else if(type.equals("game") && get4.equals("chat") && get5.equals("red")){
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GET_GAME_CHAT_RED)
                        .gameId(id)
                        .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();
            } else if(type.equals("game") && get4.equals("chat") && get5.equals("eliminated")) {
                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GET_GAME_CHAT_ELIMINATED)
                        .gameId(id)
                        .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }


            if(type.equals("game") && get4.equals("notice") && get5.equals("users")){
//                if(!customRedisRoomRepository.isUserInRoom(customRedisSessionRepository.getNicknameBySessionId(sessionId), id)){
//                    System.out.println("여기서 걸림 2/7");
//                }

                MeogajoaMessage.GameMQRequest gameMQRequest = MeogajoaMessage.GameMQRequest.builder()
                        .type(MessageType.GAME_USER_LIST)
                        .gameId(id)
                        .sender("") // 차후 조정 시 제대로 된 nickname 넣어야함 .sender(customRedisSessionRepository.getNicknameBySessionId(sessionId))
                        .content("")
                        .build();

                redisStreamGameMessagePublisher.asyncPublish(gameMQRequest);
            }
        }
    }

    private void processSubscription(String type, String id, String sessionId){
        switch(type){
            case "room":
                RoomUserInfo roomUserInfo = RoomUserInfo.from(customRedisRoomRepository.getUsersInRoom(id), id);
                redisPubSubRoomInfoPublisher.publishRoomInfo(roomUserInfo);

                break;
            case "game":
                break;
            case "user":
                break;
        }
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor, String sessionId) {

    }

    private void handleDisconnect(StompHeaderAccessor accessor, String sessionId) {
        redisAuthService.deleteSessionId(sessionId);
    }

    private void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        if (!redisAuthService.isValidSessionId(sessionId)) {
            //throw new StompException(ErrorCode.AUTH_SESSION_INVALID);
        }

        accessor.setUser(new StompPrincipal(sessionId));
        webSocketConnectionManager.addSession(customRedisSessionRepository.getNicknameBySessionId(sessionId), sessionId);
    }

}
