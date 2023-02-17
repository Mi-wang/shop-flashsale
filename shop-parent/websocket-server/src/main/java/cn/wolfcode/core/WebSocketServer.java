package cn.wolfcode.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-17 017 21:10
 */
@Setter
@Getter
@ServerEndpoint("/{token}")
@Component
public class WebSocketServer {
    public static ConcurrentHashMap<String, Session> CLIENTS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("客户端连接===>" + token);
        CLIENTS.put(token, session);
    }

    @OnClose
    public void onClose(@PathParam("token") String token) {
        CLIENTS.remove(token);
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }
}