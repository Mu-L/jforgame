package jforgame.socket.netty;

public interface WebSocketFrameType {

    /**
     * websocket frame data type -- text frame
     */
    int FRAME_TYPE_TEXT = 1;
    /**
     * websocket frame data type -- binary frame
     */
    int FRAME_TYPE_BINARY = 2;
}
