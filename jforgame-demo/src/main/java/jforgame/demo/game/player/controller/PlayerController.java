package jforgame.demo.game.player.controller;

import jforgame.demo.game.GameContext;
import jforgame.demo.game.player.message.ReqPlayerLogin;
import jforgame.socket.core.protocol.annotation.MessageRoute;
import jforgame.socket.core.protocol.annotation.RequestHandler;
import jforgame.socket.core.session.IdSession;

@MessageRoute
public class PlayerController {

    @RequestHandler
    public void reqAccountLogin(IdSession session, ReqPlayerLogin request) {
        GameContext.playerManager.handlePlayerLogin(session, request.getPlayerId());
    }

}
