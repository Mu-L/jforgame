package jforgame.socket.netty;

public interface WebSocketFrameType {

    /**
     * Server-side auto-detect mode: each client connection independently determines
     * its frame type based on the first uplink business frame.
     */
    int FRAME_TYPE_AUTO = 0;
    /**
     * websocket frame data type -- text frame
     */
    int FRAME_TYPE_TEXT = 1;
    /**
     * websocket frame data type -- binary frame
     */
    int FRAME_TYPE_BINARY = 2;
}
