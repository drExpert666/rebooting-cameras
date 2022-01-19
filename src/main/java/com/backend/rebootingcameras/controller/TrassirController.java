package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.trassir_models.TrassirChannel;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import com.backend.rebootingcameras.trassir_requests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TrassirController {

    private final static String TRASSIR_MAIN_NAME = "192.168.98.1";
    private final static String SESSION_URL_WITH_SDK = "https://" + TRASSIR_MAIN_NAME + ":8080/login?password=12345";
    private final static String SESSION_URL_WITH_USER = "https://" + TRASSIR_MAIN_NAME + ":8080/login?username=Admin&password=Tiera6778351";
    private final static String SERVERS_GUID_METHOD = "settings/network/";
    private final static String STRING_FOR_FORMAT = "https://%s:8080/%s?sid=%s";

    private TrassirSession trassirSessionWithSdk; // данные сессии через id сессии
    private TrassirSession trassirSessionWithUser; // данные сессии через имя и пароль

    private TrassirGuid trassirGuid;
    private List<TrassirServerInfo> serverInfos = new ArrayList<>();
    private List<TrassirChannel> trassirChannels = new ArrayList<>();

    private RestTemplate restTemplate; // DI для работы с запросами

    private ArrayList<String> serversIps; // список серверов

    @Autowired
    public TrassirController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    // планировщик, запускающий сбор статистики с серверов Trassir
    @Scheduled(initialDelay = 3000, fixedDelayString = "PT01H")
    public void startCollectTrassirStats() {
        if (trassirSessionWithSdk == null) {
            openSession(SESSION_URL_WITH_SDK);
            System.out.println(this.trassirSessionWithSdk);
        }
//        if (trassirSessionWithSdk.getError_code() != null) {
//            openSession(SESSION_URL_WITH_SDK);
//            System.out.println("Повторная попытка открыть сессию - ошибка: " + trassirSessionWithSdk.getError_code());
//        } else {
//            System.out.println("Сессия " + this.trassirSessionWithSdk.getSid() + " уже созана");
//            trassirServersGuid = getTrassirServersGuid();
//            System.out.println(trassirServersGuid);
//        }
//        if (trassirServersGuid != null) {
//            getServersIp(trassirServersGuid);
//            System.out.println(serverInfos);
//        }

        System.out.println("Завершено");
        getChannels(TRASSIR_MAIN_NAME, trassirSessionWithSdk.getSid());
        for (TrassirChannel trassirChannel:
             trassirChannels) {
            System.out.println(trassirChannel);
        }
    }


    // получение сессии
    private ResponseEntity<TrassirSession> openSession(String sessionURL) {
        TrassirSession trassirSessionWithSdk = restTemplate.getForObject(sessionURL, TrassirSession.class);
        this.trassirSessionWithSdk = trassirSessionWithSdk;
        return ResponseEntity.ok(trassirSessionWithSdk);
    }


    // конструктор для создания get запроса из переданных параметров
    private String createUrl(String SID, String serverIP, String method) {
        return "https://" + serverIP + ":8080/" + method + "?sid=" + SID;
    }

    // получение guid серверов
//    private TrassirGuid getTrassirServersGuid() {
//        String serversGuid = createUrl(trassirSessionWithSdk.getSid(), TRASSIR_MAIN_NAME, SERVERS_GUID_METHOD);
//        return restTemplate.getForObject(serversGuid, TrassirGuid.class);
//    }

    // получение ip серверов
    private List<TrassirServerInfo> getServersIp(TrassirGuid trassirGuid) {
        if (trassirGuid == null) {
            return null;
        }
        for (String guid : trassirGuid.getSubdirs()) {
            if (!guid.equals("network_node_add")) {
                String serverIp = createUrl(trassirSessionWithSdk.getSid(), TRASSIR_MAIN_NAME, SERVERS_GUID_METHOD + guid + "/ip_address");
                System.out.println(Thread.currentThread().getName());
                TrassirServerInfo trassirServerInfo = restTemplate.getForObject(serverIp, TrassirServerInfo.class);
                assert trassirServerInfo != null;
                trassirServerInfo.setGuid(guid);
                serverInfos.add(trassirServerInfo);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return serverInfos;
    }

    /** получение списка каналов сервера */ //todo добавить проверки
    private void getChannels(String serverIp, String SID) {

        String serversList = "settings/"; // список серверов
        String urlForServers = String.format(STRING_FOR_FORMAT, serverIp, serversList, SID);

        String channelsList = "settings/channels/"; // список каналов
        String urlForChannels = String.format(STRING_FOR_FORMAT, serverIp, channelsList, SID); // для получения массива guid каналов

        String devicesList = "settings/ip_cameras/"; // список девайсов

        TrassirChannel trassirChannel;

        /* получаем guid сервера */
        ServerGuid serverGuid = restTemplate.getForObject(urlForServers, ServerGuid.class);
        String serverGuidValue = serverGuid.getName();

        TrassirGuid channelsGUIDs = restTemplate.getForObject(urlForChannels, TrassirGuid.class);
        for (String guidChannel : channelsGUIDs.getSubdirs()) {

            /* получаем имя канала */
            String getChannelName = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/name", SID);
            ChannelName channelName = restTemplate.getForObject(getChannelName, ChannelName.class);
            if (channelName.getError_code() != null) {
                channelName.setValue("Неизвестное устройство");
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* получаем статус канала */
            String getChannelStatus = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/flags/signal", SID);
            ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String deviceGuidValue = null;

            /* получаем guid девайса */
            String getIpGuid = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/info/grabber_path", SID);
            DeviceGuid deviceGuid = restTemplate.getForObject(getIpGuid, DeviceGuid.class);
            if (deviceGuid.getValue() != null) {
                StringBuilder stringBuffer = new StringBuilder(deviceGuid.getValue());
                deviceGuidValue = stringBuffer.delete(0, 21).toString();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* получаем ip девайса */
            String deviceIpValue = null;
            if (deviceGuidValue != null) {
                String getDeviceIp = String.format(STRING_FOR_FORMAT, serverIp, devicesList + deviceGuidValue + "/connection_ip", SID);
                DeviceIp deviceIp = restTemplate.getForObject(getDeviceIp, DeviceIp.class);
                deviceIpValue = deviceIp.getValue();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* получаем модель девайса */
            String deviceModelValue = null;
            if (deviceGuidValue != null) {
                String getDeviceModel = String.format(STRING_FOR_FORMAT, serverIp, devicesList + deviceGuidValue + "/model", SID);
                DeviceIp deviceModel = restTemplate.getForObject(getDeviceModel, DeviceIp.class);
                deviceModelValue = deviceModel.getValue();
            }


            trassirChannel = new TrassirChannel(serverGuidValue,
                    guidChannel, channelName.getValue(),
                    channelStatus.getValue(),
                    deviceGuidValue, deviceIpValue, deviceModelValue);

            trassirChannels.add(trassirChannel);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
