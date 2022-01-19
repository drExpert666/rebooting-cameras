package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.ServersData;
import com.backend.rebootingcameras.trassir_models.TrassirChannel;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import com.backend.rebootingcameras.trassir_requests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class TrassirController {

    private final static String TRASSIR_MAIN_NAME = "192.168.98.1";
    private final static String SESSION_URL_WITH_SDK = "https://" + TRASSIR_MAIN_NAME + ":8080/login?password=12345";
    private final static String SESSION_URL_WITH_USER = "https://" + TRASSIR_MAIN_NAME + ":8080/login?username=Admin&password=Tiera6778351";
    private final static String SERVERS_GUID_METHOD = "settings/network/";
    private final static String STRING_FOR_FORMAT = "https://%s:8080/%s?sid=%s";
    private final static String STRING_FOR_SESSION = "https://%s:8080/login?password=12345";

    private TrassirSession trassirSessionWithSdk; // данные сессии через id сессии
    private TrassirSession trassirSessionWithUser; // данные сессии через имя и пароль

    private TrassirGuid trassirGuid;

    private ArrayList<TrassirChannel> trassirChannels = new ArrayList<>(); // список каналов
    private ArrayList<TrassirServerInfo> servers; // список серверов

    private RestTemplate restTemplate; // DI для работы с запросами


    @Autowired
    public TrassirController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    // планировщик, запускающий сбор статистики с серверов Trassir
    @Scheduled(initialDelay = 3000, fixedDelayString = "PT11S")
    public void startCollectTrassirStats() {

        System.out.println("Начало сбора информации о серверах");
        System.out.println("--------------------------------------------------");
        fillServers();
        System.out.println("Информация собрана:");
        for (TrassirServerInfo serverInfo : servers) {
            System.out.println(serverInfo);
        }

        System.out.println("Начало сбора информации о камерах");
        System.out.println("--------------------------------------------------");
        if (servers != null) {
            for (TrassirServerInfo serverInfo : servers) {
                getChannels(serverInfo.getServerIP(), serverInfo.getSessionId());
            }
        }

        for (TrassirChannel trassirChannel : trassirChannels) {
            System.out.println(trassirChannel);
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Сбор всей информации завершён");

    }

    /**
     * получение списка каналов сервера
     */ //todo добавить проверки
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
                Thread.sleep(20);
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
                Thread.sleep(20);
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
                    deviceGuidValue, deviceIpValue, deviceModelValue, new Date());

            trassirChannels.add(trassirChannel);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * заполнение информации о серверах
     */ //todo добавить проверки
    private void fillServers() {
        ServersData serversData = new ServersData();
        servers = serversData.getServers();

        // передаём в цикле значения сервера
        for (TrassirServerInfo serverInfo : servers) {

            // получаю сессию
            String getSessionUrl = String.format(STRING_FOR_SESSION, serverInfo.getServerIP());
            TrassirSession session = restTemplate.getForObject(getSessionUrl, TrassirSession.class);

            // заполняю данные состояния
            if (session.getSid() != null) {
                serverInfo.setLustUpdate(new Date());
                String sessionId = session.getSid();
                serverInfo.setSessionId(sessionId);
                String getServerHealth = String.format(STRING_FOR_FORMAT, serverInfo.getServerIP(), "health", sessionId);
                ServerHealth serverHealth = restTemplate.getForObject(getServerHealth, ServerHealth.class);

                if (serverHealth.getError_code() == null) {
                    serverInfo.setChannels_total(serverHealth.getChannels_total());
                    serverInfo.setChannels_online(serverHealth.getChannels_online());
                    serverInfo.setServerStatus(serverHealth.getNetwork());
                }
            }

            // заполняю имя сервера
            if (session.getSid() != null) {
                String sessionId = session.getSid();
                String getServerName = String.format(STRING_FOR_FORMAT, serverInfo.getServerIP(), "settings/name", sessionId);
                ServerName serverName = restTemplate.getForObject(getServerName, ServerName.class);

                if (serverName.getError_code() == null) {
                    serverInfo.setServerName(serverName.getValue());
                }
            }

            // усыпляем поток перед следующим вызовом
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
