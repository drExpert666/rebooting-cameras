package com.backend.rebootingcameras.data;

/** класс-контейнер для хранения строковых значений в переменных */
public class PathForRequest {

    /* заполнение данных о камерах и серверах */
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

    /* заполнение данных о юзерах и правах юзера */
    public final static String STRING_USER_LIST =  "settings/users/"; //список юзеров на сервере
    public final static String STRING_BASE_RIGHTS =  "base_rights"; //список базовых прав юзера
    public final static String STRING_ACL_RIGHTS =  "acl"; //список дополнительных прав юзера

    public final static int TIME_BETWEEN_NOW_AND_GET_SESSION =  780000; // время милисекундах (13 минут)

    /* данные для обращеня к коммутаторам */
    public final static String STRING_FOR_REBOOT_CISCO_UBUNTU ="snmpset -v 2c -c public %s .1.3.6.1.2.1.105.1.1.1.3.1.%s i %s";

    public final static String STRING_FOR_REBOOT_CISCO = "-v:2c -c:public -r:%s -o:.1.3.6.1.2.1.105.1.1.1.3.1.%s -val:%s -tp:int";
    public final static String STRING_FOR_RUN_SNMP_SET = "C:\\Users\\romanov-av\\Downloads\\SnmpSet\\SnmpSet.exe ";

    public final static char STRING_FOR_REBOOT_OFF = '2';
    public final static char STRING_FOR_REBOOT_ON = '1';

    public final static String SWITCH_MODEL_CISCO_SG200 =  "CISCO SG200-26P 26-Port Gigabit PoE Smart Switch";
    public final static String SWITCH_MODEL_CISCO_SG300 =  "CISCO SG300-28P 28-Port Gigabit PoE Managed Switch";
    public final static String SWITCH_MODEL_CISCO_SFE2000P =  "CISCO SFE2000P 24-port 10/100 Ethernet Switch with PoE";

    public final static String DEFAULT_SORT_DIRECTION_COLUMN =  "asc";
    public final static String OPTIONAL_SORT_DIRECTION_COLUMN =  "desc";

    public final static String DEFAULT_SORT_COLUMN =  "guidServer";


}
