package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.ServersData;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import com.backend.rebootingcameras.trassir_requests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class TrassirController {

    private final static String STRING_FOR_FORMAT = "https://%s:8080/%s?sid=%s";
    private final static String STRING_FOR_SESSION = "https://%s:8080/login?password=12345";

    private TrassirGuid trassirGuid;

    private ServersData serversData; // массив серверов с данными (пока заменяет БД)
    private List<TrassirChannelInfo> trassirChannels = new ArrayList<>(); // список каналов (пока заменяет БД)
    private List<TrassirServerInfo> servers; // список серверов (пока заменяет БД)

    private RestTemplate restTemplate; // DI для работы с запросами
    private TrassirChannelService trassirChannelService;
    private TrassirServerService trassirServerService;

    @Autowired
    public void setTrassirServerService(TrassirServerService trassirServerService) {
        this.trassirServerService = trassirServerService;
    }

    @Autowired
    public TrassirController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setTrassirChannelService(TrassirChannelService trassirChannelService) {
        this.trassirChannelService = trassirChannelService;
    }

    /**
     * планировщик, запускающий сбор статистики с серверов Trassir
     */
    @Scheduled(initialDelay = 1000, fixedDelayString = "PT5S")
    public void startCollectTrassirStats() {

        System.out.println("Начало сбора информации о серверах");
        System.out.println("--------------------------------------------------");
        fillServers();
        System.out.println("Информация собрана:");
        for (TrassirServerInfo serverInfo : servers) {
            System.out.println(serverInfo);
        }

        updateAllServers(servers);

        System.out.println("Начало сбора информации о камерах");
        System.out.println("--------------------------------------------------");
        if (servers != null) {
            for (TrassirServerInfo serverInfo : servers) {
                getChannels(serverInfo);
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Сбор всей информации завершён");

        updateAllChannels(trassirChannels);

    }

    /**
     * заполнение информации о серверах
     */
    private void fillServers() {

        if (serversData == null) { // если первый запуск приложения
            serversData = new ServersData();
        }
        if (servers == null) {// если первый запуск приложения
            servers = serversData.getServers();
        }

        // присваиваем в цикле значения для каждого сервера сервера
        for (TrassirServerInfo serverInfo : servers) {

            /* получаю сессию */
            String getSessionUrl = String.format(STRING_FOR_SESSION, serverInfo.getServerIP());  // строка для получения сессии
            TrassirSession session = restTemplate.getForObject(getSessionUrl, TrassirSession.class);

            serverInfo.setLustUpdate(new Date()); // время последнего обновления

            /* заполняю данные состояния */
            if (session != null && session.getSid() != null) {
                String sessionId = session.getSid();
                serverInfo.setSessionId(sessionId);
                String getServerHealth = String.format(STRING_FOR_FORMAT, serverInfo.getServerIP(), "health", sessionId);
                ServerHealth serverHealth = restTemplate.getForObject(getServerHealth, ServerHealth.class);

                if (serverHealth != null && serverHealth.getError_code() == null) {
                    serverInfo.setChannels_total(serverHealth.getChannels_total());
                    serverInfo.setChannels_online(serverHealth.getChannels_online());
                    serverInfo.setServerStatus(serverHealth.getNetwork());
                }
                /* усыпляем поток перед следующим вызовом */
                threadSleepWithTryCatchBlock(20);

                /* заполняю имя сервера */
                String getServerName = String.format(STRING_FOR_FORMAT, serverInfo.getServerIP(), "settings/name", sessionId);
                ServerName serverName = restTemplate.getForObject(getServerName, ServerName.class);

                if (serverName != null && serverName.getError_code() == null) {
                    serverInfo.setServerName(serverName.getValue());
                }

            } else {
                serverInfo.setSessionId(null);
                serverInfo.setChannels_total(null);
                serverInfo.setChannels_online(null);
                serverInfo.setServerStatus(-1); //todo проверить, какое значение означает отсутствие связи с сервером
            }

            /* усыпляем поток перед следующим вызовом */
            threadSleepWithTryCatchBlock(20);
        }
    }


    /**
     * получение списка каналов сервера
     */
    private void getChannels(TrassirServerInfo serverInfo) {

        String serverIp = serverInfo.getServerIP();
        String SID = serverInfo.getSessionId();

        /* создаём url для получения guid сервера */
        final String serversList = "settings/"; // список серверов
        String urlForServers = String.format(STRING_FOR_FORMAT, serverIp, serversList, SID);

        final String channelsList = "settings/channels/"; // список каналов
        String urlForChannels = String.format(STRING_FOR_FORMAT, serverIp, channelsList, SID); // для получения массива guid каналов

        final String devicesList = "settings/ip_cameras/"; // список девайсов

        /* получаем массив guid каналов сервера */
        TrassirGuid channelsGUIDs = restTemplate.getForObject(urlForChannels, TrassirGuid.class);

        /* получаем guid сервера */
        String serverGuid = null;
        for (TrassirServerInfo s : servers) {
            if (s.getServerIP().equals(serverIp)) {
                serverGuid = s.getGuid();
            }
        }

        if (channelsGUIDs != null) {

            /* если список каналов пустой (приложение запускается в первый раз) */
            for (String guidChannel : channelsGUIDs.getSubdirs()) {

                /* получаем статус канала */
                Integer channelStatus = getChannelStatus(guidChannel, serverIp, channelsList, SID);

                /* получаем имя канала */
                String channelName = getChannelName(guidChannel, serverIp, channelsList, SID);

                /* получаем guid девайса */ //todo добавить проверки дальше
                String deviceGuidValue = null;
                String getIpGuid = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/info/grabber_path", SID);
                DeviceGuid deviceGuid = restTemplate.getForObject(getIpGuid, DeviceGuid.class);
                if (deviceGuid.getValue() != null) {
                    StringBuilder stringBuffer = new StringBuilder(deviceGuid.getValue());
                    deviceGuidValue = stringBuffer.delete(0, 21).toString();
                }

                threadSleepWithTryCatchBlock(20);

                /* получаем ip девайса */
                String deviceIpValue = null;
                if (deviceGuidValue != null) {
                    String getDeviceIp = String.format(STRING_FOR_FORMAT, serverIp, devicesList + deviceGuidValue + "/connection_ip", SID);
                    DeviceIp deviceIp = restTemplate.getForObject(getDeviceIp, DeviceIp.class);
                    deviceIpValue = deviceIp.getValue();
                }

                threadSleepWithTryCatchBlock(20);

                /* получаем модель девайса */
                String deviceModelValue = null;
                if (deviceGuidValue != null) {
                    String getDeviceModel = String.format(STRING_FOR_FORMAT, serverIp, devicesList + deviceGuidValue + "/model", SID);
                    DeviceIp deviceModel = restTemplate.getForObject(getDeviceModel, DeviceIp.class);
                    deviceModelValue = deviceModel.getValue();
                }


                TrassirChannelInfo trassirChannel = new TrassirChannelInfo(serverGuid,
                        guidChannel, channelName,
                        channelStatus,
                        deviceGuidValue, deviceIpValue, deviceModelValue, new Date());

                trassirChannels.add(trassirChannel);
                System.out.println(trassirChannel);

                threadSleepWithTryCatchBlock(20);

            }

        }

//            else {
//                List<TrassirChannel> newTrassirChannels = new ArrayList<>();
//                // для каждого элемента из записанного в массив (БД) делаем проверку:
//                trassirChannels.forEach(trassirChannel -> {
//
//                    trassirChannel.setLustUpdate(new Date());
//
//                    if (Arrays.asList(channelsGUIDs.getSubdirs()).contains(trassirChannel.getGuidChannel())) {
//                        trassirChannel.setSignal(getChannelStatus(trassirChannel.getGuidChannel(), serverIp, channelsList, SID));
//
//                        if (trassirChannel.getName() == null) {
//                            trassirChannel.setName(getChannelName(trassirChannel.getGuidChannel(), serverIp, channelsList, SID));
//                        }
//                    } else {
//                        trassirChannel.setSignal(-1);
//                    }
//
//                    newTrassirChannels.add(trassirChannel);
//
//                });
//                trassirChannels = newTrassirChannels;
//            }



}

    /* если данные однажды уже были получены */
//        else {
//            if (channelsGUIDs != null) {
//                for (String guidChannel : channelsGUIDs.getSubdirs()) {
//                    trassirChannels.forEach(channel -> {
//
//                        // запрашиваем статус
//                        String getChannelStatus = String.format(STRING_FOR_FORMAT, serverIp, channelsList + channel.getGuidChannel() + "/flags/signal", SID);
//                        ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);
//
//                        // проверяем, есть ли такой guid в массиве и есть ли у него сигнал
//                        if (channel.getGuidChannel().equals(guidChannel)) {
//
//                            if (channelStatus == null || channelStatus.getError_code() != null) {
//                                channel.setSignal(-1); // если нет связи, то ставим null //todo или -1, нужно проверить
//                            } else {
//                                channel.setSignal(channelStatus.getValue()); // если есть связь, то обновляем статус сигнала
//                            }
//                            channel.setLustUpdate(new Date());
//
//                        } else { // такого guid нет в массиве (либо новый элемент, либо удалён (отсутсвует сигнал))
//
//                            if (channelStatus != null && channelStatus.getError_code() == null) { // значит камера подключена, но не добавлена в БД (массив)
//                                //todo добавить методы по добавлению нового канала, пока оставлю заглушку:
//                                trassirChannels.add(new TrassirChannel(serverIp, guidChannel, null,null,null,null, null, new Date()));
//                            } else {
//                                channel.setSignal(null); // скорее всего камера удалена из трассира
//                            }
//
//
//                        }
//                    });
//
//
//                    ;
//                    {
//
//                        /* изменяем состояние (если поменялось) для каждого канала */
////                        String getChannelStatus = String.format(STRING_FOR_FORMAT, serverIp, channelsList + channel.getGuidChannel() + "/flags/signal", SID);
////                        ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);
////                        if (channelStatus == null || channelStatus.getError_code() != null) {
////                            channelStatus = new ChannelStatus();
////                            channelStatus.setValue(null);
////                        }
//
//
//                    }
//                }
//            }
//
//        }


    // вынесенное в отдельный метод усыпление потока перед новым запросом в Трассир
    private void threadSleepWithTryCatchBlock(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /* получаем имя канала */
    private String getChannelName(String guidChannel, String serverIp, String channelsList, String SID) {

        String getChannelName = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/name", SID);
        ChannelName channelName = restTemplate.getForObject(getChannelName, ChannelName.class);
        if (channelName == null || channelName.getError_code() != null) {
            channelName = new ChannelName();
            channelName.setValue("Неизвестное устройство");
        }
        threadSleepWithTryCatchBlock(20);
        return channelName.getValue();
    }

    /* получаем статус канала */
    private Integer getChannelStatus(String guidChannel, String serverIp, String channelsList, String SID) {
        String getChannelStatus = String.format(STRING_FOR_FORMAT, serverIp, channelsList + guidChannel + "/flags/signal", SID);
        ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);
        if (channelStatus == null || channelStatus.getError_code() != null) {
            channelStatus = new ChannelStatus();
            channelStatus.setValue(null);
        }
        threadSleepWithTryCatchBlock(20);
        return channelStatus.getValue();
    }

    List<TrassirChannelInfo> updateAllChannels(List<TrassirChannelInfo> channels) {
       return trassirChannelService.updateAll(channels);
    }

    List<TrassirServerInfo> updateAllServers(List<TrassirServerInfo> servers) {
        return trassirServerService.updateAll(servers);
    }

}
