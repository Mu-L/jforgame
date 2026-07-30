package jforgame.demo;

import jforgame.demo.utils.XmlUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Root(name = "server")
public class ServerConfig {

    private Logger logger = LoggerFactory.getLogger(ServerConfig.class.getSimpleName());

    private static volatile ServerConfig instance;

    /**
     * 服务器id
     */
    @Element(required = true)
    private int serverId;
    /**
     * 服务器端口
     */
    @Element(required = true)
    private int serverPort;
    /**
     * 客户端封包最大字节数
     */
    @Element(required = true)
    private int maxReceiveBytes;

    /**
     * 本服是否為跨服
     */
    @Element(required = true)
    private int fight;
    /**
     * 对外跨服端口
     */
    @Element(required = true)
    private int crossPort;

    private ServerConfig() {
    }

    public static ServerConfig getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (ServerConfig.class) {
            if (instance == null) {
                instance = XmlUtils.loadXmlConfig("server.xml", ServerConfig.class);
                instance.init();
            }
        }
        return instance;
    }

    private void init() {
        logger.info("本服serverId为{}", this.serverId);
    }

    public int getServerId() {
        return this.serverId;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * 该服为战斗服
     *
     * @return
     */
    public boolean isFight() {
        return fight == 1;
    }

    /**
     * 该服为匹配中心服
     *
     * @return
     */
    public boolean isCenter() {
        return fight == 2;
    }

    public int getCrossPort() {
        return crossPort;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public void setCrossPort(int crossPort) {
        this.crossPort = crossPort;
    }

    public int getMaxReceiveBytes() {
        return maxReceiveBytes;
    }

    public void setMaxReceiveBytes(int maxReceiveBytes) {
        this.maxReceiveBytes = maxReceiveBytes;
    }

    public String getMatchUrl() {
        return "";
    }

}
