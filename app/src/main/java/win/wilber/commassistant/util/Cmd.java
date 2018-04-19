package win.wilber.commassistant.util;

public class Cmd {
    public static final String MODE_AT = "AT";
    public static final String ENABLE_ECHO = "AT+ECHO=%d";
    public static final String CONFIG_WORK_MODE = "AT+C1_OP=%d";
    public static final String DHCP = "AT+IP_MODE=%s";
    public static final String CONFIG_LOCAL_PORT = "AT+C1_PORT=%s";
    public static final String CONFIG_REMOTE_IP = "AT+C1_CLI_IP1=%s";
    public static final String CONFIG_REMOTE_PORT = "AT+C1_CLI_PP1=%s";
    public static final String CONFIG_LOCAL_IP = "AT+IP=%s";
    public static final String CONFIG_LOCAL_MASK = "AT+MARK=%s";
    public static final String CONFIG_LOCAL_GATEWAY = "AT+GATEWAY=%s";
    public static final String EXIT_MODE_AT = "AT+EXIT";
    public static final String RESET = "AT+DEFAULT=%s";

    public static final int MODE_TCPSERVER = 0;
    public static final int MODE_TCPCLIENT = 1;
    public static final int MODE_UDP = 2;

    public static final int MODE_STATIC_IP = 0;
    public static final int MODE_DHCP = 1;


    public static String enableEcho(boolean enable) {
        return String.format(ENABLE_ECHO,enable ? 1 : 0);
    }

    /**
     * @param mode {@link #MODE_TCPSERVER}, {@link #MODE_AT}, {@link #MODE_TCPCLIENT}
     * @return
     */
    public static String configWorkMode(int mode) {
        return String.format(CONFIG_WORK_MODE,mode);
    }

    /**
     * @param mode {@link #MODE_STATIC_IP}, {@link #MODE_DHCP}
     * @param mode
     * @return
     */
    public static String configDhcpMode(int mode) {
        return String.format(DHCP,mode);
    }

    public static String getLocalPort(String port) {
        return String.format(CONFIG_LOCAL_PORT,port);
    }

    public static String getRemoteIp(String ip) {
        return String.format(CONFIG_REMOTE_IP,ip);
    }

    public static String getRemotePort(String port) {
        return String.format(CONFIG_REMOTE_PORT,port);
    }

    public static String getLocalIp(String ip) {
        return String.format(CONFIG_LOCAL_IP,ip);
    }

    public static String getLocalMask(String netmask) {
        return String.format(CONFIG_LOCAL_MASK,netmask);
    }

    public static String getLocalGateway(String gateway) {
        return String.format(CONFIG_LOCAL_GATEWAY,gateway);
    }

    public static String reset(String passwd) {
        return String.format(RESET,passwd);
    }

    public static final String EXIT_MODE_DATA = "+++";
    public static final String GET_VERSION = "AT+VER?";
    public static final String GET_RUN_TIME = "AT+RUNTIME?";
    public static final String GET_RCV_BYTES = "AT+NETRCV?";
    public static final String GET_SEND_BYTES = "AT+NETSEND?";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";
//    public static final String  = "";

}
