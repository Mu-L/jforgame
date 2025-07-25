package jforgame.socket.netty.support.server;

import jforgame.codec.MessageCodec;
import jforgame.socket.netty.support.ChannelIoHandler;
import jforgame.socket.share.ChainedMessageDispatcher;
import jforgame.socket.share.HostAndPort;
import jforgame.socket.share.message.MessageFactory;

import java.io.File;

public class WebSocketServerBuilder {

    public static WebSocketServerBuilder newBuilder() {
        return new WebSocketServerBuilder();
    }

    private HostAndPort hostPort;
    private MessageFactory messageFactory;
    private MessageCodec messageCodec;
    private ChainedMessageDispatcher socketIoDispatcher;
    private String websocketPath = "/ws";
//    private SslContext sslContext;
//    private boolean enableSsl = false; // 默认不启用SSL
//    private boolean useSelfSignedCert = true; // 是否使用自签名证书
    private String certDomain; // 证书域名
    private File certChainFile; // 证书链文件
    private File privateKeyFile; // 私钥文件
    private String keyPassword; // 私钥密码

    /**
     * In the server side, the connection will be closed if it is idle for a certain period of time.
     * unit is MILLISECONDS
     */
    private int idleMilliSeconds;

    public WebSocketServerBuilder setSocketIoDispatcher(ChainedMessageDispatcher socketIoDispatcher) {
        this.socketIoDispatcher = socketIoDispatcher;
        return this;
    }

    public WebSocketServerBuilder setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
        return this;
    }

    public WebSocketServerBuilder setMessageCodec(MessageCodec messageCodec) {
        this.messageCodec = messageCodec;
        return this;
    }

    public WebSocketServerBuilder setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
        return this;
    }

    public WebSocketServerBuilder bindingPort(HostAndPort hostPort) {
        this.hostPort = hostPort;
        return this;
    }

    public WebSocketServerBuilder setIdleMilliSeconds(int idleMilliSeconds) {
        this.idleMilliSeconds = idleMilliSeconds;
        return this;
    }

//    /**
//     * 启动自签名证书
//     *
//     * @param domain 证书域名
//     * @return
//     */
//    public WebSocketServerBuilder useSelfSignedCertificate(String domain) {
//        this.enableSsl = true; // 自动启用SSL
//        this.useSelfSignedCert = true;
//        this.certDomain = domain;
//        return this;
//    }
//
//    /**
//     * 启动正式证书
//     *
//     * @param certChainFile  证书文件
//     * @param privateKeyFile 私钥文件
//     */
//    public WebSocketServerBuilder useFormalCertificate(File certChainFile, File privateKeyFile) {
//        return useFormalCertificate(certChainFile, privateKeyFile, null);
//    }
//
//    /**
//     * 启动正式证书
//     *
//     * @param certChainFile  证书文件
//     * @param privateKeyFile 私钥文件
//     * @param keyPassword    私钥密码，如果没有则传null
//     */
//    public WebSocketServerBuilder useFormalCertificate(File certChainFile, File privateKeyFile, String keyPassword) {
//        this.enableSsl = true; // 自动启用SSL
//        this.useSelfSignedCert = false;
//        this.certChainFile = certChainFile;
//        this.privateKeyFile = privateKeyFile;
//        this.keyPassword = keyPassword;
//        return this;
//    }
//
//    /**
//     * 直接设置SSL上下文
//     * 预留接口，用于高级用户手动设置SSL上下文
//     *
//     * @param sslContext
//     */
//    public WebSocketServerBuilder setSslContext(SslContext sslContext) {
//        this.enableSsl = true; // 自动启用SSL
//        this.sslContext = sslContext;
//        return this;
//    }

    public WebSocketServer build() {
        // 验证必要参数
        if (socketIoDispatcher == null) {
            throw new IllegalArgumentException("socketIoDispatcher must not null");
        }
        if (messageFactory == null) {
            throw new IllegalArgumentException("messageFactory must not null");
        }
        if (messageCodec == null) {
            throw new IllegalArgumentException("messageCodec must not null");
        }
        if (hostPort == null) {
            throw new IllegalArgumentException("hostPort must not null");
        }

        // 配置SSL上下文
//        if (enableSsl) {
//            if (sslContext == null) {
//                try {
//                    if (useSelfSignedCert) {
//                        // 使用自签名证书
//                        SelfSignedCertificate ssc = certDomain != null ?
//                                new SelfSignedCertificate(certDomain) :
//                                new SelfSignedCertificate();
//                        sslContext = SslContextBuilder
//                                .forServer(ssc.certificate(), ssc.privateKey())
//                                .build();
//                    } else {
//                        // 使用正式证书
//                        if (certChainFile == null || privateKeyFile == null) {
//                            throw new IllegalArgumentException("certChainFile and privateKeyFile must not null when using formal certificate");
//                        }
//                        SslContextBuilder builder = SslContextBuilder.forServer(certChainFile, privateKeyFile);
//                        if (keyPassword != null) {
//                            builder.keyManager(certChainFile, privateKeyFile, keyPassword);
//                        }
//                        sslContext = builder.build();
//                    }
//                } catch (CertificateException | SSLException e) {
//                    throw new RuntimeException("Failed to initialize SSL context", e);
//                }
//            }
//        }

        // 创建并配置服务器实例
        WebSocketServer socketServer = new WebSocketServer();
//        socketServer.sslContext = sslContext;
        socketServer.nodeConfig = hostPort;
        socketServer.messageCodec = messageCodec;
        socketServer.messageFactory = messageFactory;
        socketServer.messageIoHandler = new ChannelIoHandler(socketIoDispatcher);
        socketServer.socketIoDispatcher = socketIoDispatcher;
        socketServer.websocketPath = websocketPath;
        socketServer.idleMilliSeconds = idleMilliSeconds;

        return socketServer;
    }
}