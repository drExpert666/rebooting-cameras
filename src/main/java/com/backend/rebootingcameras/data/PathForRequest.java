package com.backend.rebootingcameras.data;

public class PathForRequest {

    public final static String STRING_FOR_FORMAT = "https://%s:8080/%s?sid=%s";
    public final static String STRING_FOR_SESSION = "https://%s:8080/login?password=12345";
    public final static String STRING_SERVER_HEALTH = "health";
    public final static String STRING_SERVER_NAME = "settings/name";
    public final static String STRING_CHANNEL_FLAG_SIGNAL = "/flags/signal";
    public final static String STRING_CHANNEL_LIST = "settings/channels/";
    public final static String STRING_CHANNEL_NAME ="/name";
    public final static String STRING_DEVICE_GUID = "/info/grabber_path";
    public final static String STRING_DEVICE_LIST = "settings/ip_cameras/"; // список девайсов
    public final static String STRING_DEVICE_IP = "/connection_ip";
    public final static String STRING_DEVICE_MODEL =  "/model";

    public final static int TIME_BETWEEN_NOW_AND_GET_SESSION =  780000; // время милисекундах (13 минут)

    public final static String STRING_FOR_REBOOT_CISCO = "-v:2c -c:public -r:%s -o:.1.3.6.1.2.1.105.1.1.1.3.1.%s -val:%s -tp:int";
    public final static String STRING_FOR_RUN_SNMP_SET = "C:\\Users\\romanov-av\\Downloads\\SnmpSet\\SnmpSet.exe ";

}
