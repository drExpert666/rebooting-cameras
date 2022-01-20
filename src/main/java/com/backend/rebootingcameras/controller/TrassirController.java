package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
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

    private TrassirGuid trassirGuid;

    private ServersData serversData; // массив серверов с данными (пока заменяет БД)

    private List<TrassirChannelInfo> channelsFromTrassir; // список каналов (из запроса к Трассиру)
    private List<TrassirChannelInfo> channelsFromDB; // список (из БД)

    private List<TrassirServerInfo> servers; // список серверов (пока заменяет БД)

    /* DI */
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

        /* получение данных из БД */
        channelsFromDB = findAllCameras();

        fillServers();

        updateAllServers(servers);

        /* заполняем в цикле список каналов из Трассира */
        channelsFromTrassir = new ArrayList<>();
        if (servers != null) {
            for (TrassirServerInfo serverInfo : servers) {
                getChannels(serverInfo);
            }
        }

        if (channelsFromDB == null && channelsFromTrassir != null) {
            updateAllChannels(channelsFromTrassir);
        }

        if (channelsFromDB != null && channelsFromTrassir != null) {
            updateAllChannelsWithCheckingFields(channelsFromTrassir);
        }

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
            String getSessionUrl = String.format(PathForRequest.STRING_FOR_SESSION, serverInfo.getServerIP());  // строка для получения сессии
            TrassirSession session = restTemplate.getForObject(getSessionUrl, TrassirSession.class);

            serverInfo.setLustUpdate(new Date()); // время последнего обновления

            /* заполняю данные состояния */
            if (session != null && session.getSid() != null) {
                String sessionId = session.getSid();
                serverInfo.setSessionId(sessionId);
                String getServerHealth = String.format(PathForRequest.STRING_FOR_FORMAT, serverInfo.getServerIP(), PathForRequest.STRING_SERVER_HEALTH, sessionId);
                ServerHealth serverHealth = restTemplate.getForObject(getServerHealth, ServerHealth.class);

                if (serverHealth != null && serverHealth.getError_code() == null) {
                    serverInfo.setChannels_total(serverHealth.getChannels_total());
                    serverInfo.setChannels_online(serverHealth.getChannels_online());
                    serverInfo.setServerStatus(serverHealth.getNetwork());
                }
                /* усыпляем поток перед следующим вызовом */
                threadSleepWithTryCatchBlock(20);

                /* заполняю имя сервера */
                String getServerName = String.format(PathForRequest.STRING_FOR_FORMAT, serverInfo.getServerIP(), PathForRequest.STRING_SERVER_NAME, sessionId);
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
     * получение списка каналов сервера их Трассира
     */
    private void getChannels(TrassirServerInfo serverInfo) {

        String serverIp = serverInfo.getServerIP();
        String SID = serverInfo.getSessionId();

        /* создаём url для получения guid канала */
        String urlForChannels = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp, PathForRequest.STRING_CHANNEL_LIST, SID); // для получения массива guid каналов

        final String devicesList = "settings/ip_cameras/"; // список девайсов

        /* получаем массив guid каналов сервера */
        TrassirGuid channelsGUIDs = restTemplate.getForObject(urlForChannels, TrassirGuid.class);

        /* получаем guid сервера */
        String serverGuid = serverInfo.getGuid();

        if (channelsGUIDs != null) {

            /* получаем список каналов из Трассира */
            for (String guidChannel : channelsGUIDs.getSubdirs()) {

                /* получаем статус канала */
                Integer channelStatus = getChannelStatus(guidChannel, serverIp, SID);

                //todo проверить какие поля можно получить при статусе -1
                if (channelStatus == null || channelStatus == -1) { // если сигнала нет, то нет смысла выполнять последующие проверки
                    TrassirChannelInfo trassirChannel = new TrassirChannelInfo(serverGuid,
                            guidChannel, null,
                            channelStatus,
                            null, null, null, new Date());
                    channelsFromTrassir.add(trassirChannel);
                } else {
                    /* получаем имя канала */
                    String channelName = getChannelName(guidChannel, serverIp, SID);

                    /* получаем guid девайса */
                    String deviceGuidValue = getChannelGuid(guidChannel, serverIp, SID);

                    /* получаем ip девайса */
                    String deviceIpValue = getDeviceIp(serverIp, deviceGuidValue, SID);

                    /* получаем модель девайса */
                    String deviceModelValue = getDeviceModel(serverIp, deviceGuidValue, SID);


                    TrassirChannelInfo trassirChannel = new TrassirChannelInfo(serverGuid,
                            guidChannel, channelName,
                            channelStatus,
                            deviceGuidValue, deviceIpValue, deviceModelValue, new Date());

                    channelsFromTrassir.add(trassirChannel);

                }

                threadSleepWithTryCatchBlock(20);

            }

        }

    }


    /* вынесенное в отдельный метод усыпление потока перед новым запросом в Трассир */
    private void threadSleepWithTryCatchBlock(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * методы работы с каналами
     */

    /* получаем имя канала */
    private String getChannelName(String guidChannel, String serverIp, String SID) {

        String getChannelName = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp, PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_CHANNEL_NAME, SID);
        ChannelName channelName = restTemplate.getForObject(getChannelName, ChannelName.class);
        if (channelName == null || channelName.getError_code() != null) {
            channelName = new ChannelName();
            channelName.setValue("Неизвестное устройство");
        }
        threadSleepWithTryCatchBlock(20);
        return channelName.getValue();
    }

    /* получаем статус канала */
    private Integer getChannelStatus(String guidChannel, String serverIp, String SID) {
        String getChannelStatus = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp, PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_CHANNEL_FLAG_SIGNAL, SID);
        ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);
        if (channelStatus == null || channelStatus.getError_code() != null) {
            channelStatus = new ChannelStatus();
            channelStatus.setValue(null);
        }
        threadSleepWithTryCatchBlock(20);
        return channelStatus.getValue();
    }

    /* получаем guid девайса */
    private String getChannelGuid(String guidChannel, String serverIp, String SID) {
        String deviceGuidValue;
        String getDeviceGuid = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp, PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_DEVICE_GUID, SID);
        DeviceGuid deviceGuid = restTemplate.getForObject(getDeviceGuid, DeviceGuid.class);
        if (deviceGuid == null || deviceGuid.getError_code() != null) {
            threadSleepWithTryCatchBlock(20);
            return null;
        } else {
            StringBuilder stringBuffer = new StringBuilder(deviceGuid.getValue());
            deviceGuidValue = stringBuffer.delete(0, 21).toString();
            threadSleepWithTryCatchBlock(20);
            return deviceGuidValue;
        }
    }

    /* получаем ip девайса */
    private String getDeviceIp(String serverIp, String deviceGuidValue, String SID) {
        String deviceIpValue;
        if (deviceGuidValue != null) {
            String getDeviceIp = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                    PathForRequest.STRING_DEVICE_LIST + deviceGuidValue + PathForRequest.STRING_DEVICE_IP, SID);
            DeviceIp deviceIp = restTemplate.getForObject(getDeviceIp, DeviceIp.class);
            if (deviceIp == null || deviceIp.getError_code() != null) {
                deviceIpValue = null;
            } else {
                deviceIpValue = deviceIp.getValue();
            }

            threadSleepWithTryCatchBlock(20);
            return deviceIpValue;

        } else {
            threadSleepWithTryCatchBlock(20);
            return null;
        }
    }

    /* получаем модель девайса */
    private String getDeviceModel(String serverIp, String deviceGuidValue, String SID) {
        String deviceModelValue;
        if (deviceGuidValue != null) {
            String getDeviceModel = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                    PathForRequest.STRING_DEVICE_LIST + deviceGuidValue + PathForRequest.STRING_DEVICE_MODEL, SID);
            DeviceModel deviceModel = restTemplate.getForObject(getDeviceModel, DeviceModel.class);
            if (deviceModel == null || deviceModel.getError_code() != null) {
                deviceModelValue = null;
            } else {
                deviceModelValue = deviceModel.getValue();
            }

            threadSleepWithTryCatchBlock(20);
            return deviceModelValue;

        } else {
            threadSleepWithTryCatchBlock(20);
            return null;
        }
    }

    /**
     * методы работы с БД каналов
     */

    /* записываем в БД список серверов */
    List<TrassirServerInfo> updateAllServers(List<TrassirServerInfo> servers) {
        return trassirServerService.updateAll(servers);
    }

    /* записываем в БД список камер */ //todo убрать возвращаемы тип, если нигде не понадобится
    List<TrassirChannelInfo> updateAllChannels(List<TrassirChannelInfo> channels) {
        return trassirChannelService.updateAll(channels);
    }

    List<TrassirChannelInfo> findAllCameras() {
        return trassirChannelService.findAll();
    }

    /* обновление всех данных с проверкой параметров */ //todo убрать возвращаемы тип, если нигде не понадобится
    private List<TrassirChannelInfo> updateAllChannelsWithCheckingFields(List<TrassirChannelInfo> channels) {
        for (TrassirChannelInfo trassirChannel : channels) {

            TrassirChannelInfo trassirChannelTmpl = trassirChannelService.findByGuid(trassirChannel.getGuidChannel());

            if (trassirChannelTmpl == null) { // если элемент в БД не найден, значит это новая запись (камера)
                trassirChannelService.updateByChannel(trassirChannel); // добавляем её в БД
            } else {

                Integer tmpSignal = trassirChannelTmpl.getSignal(); // считываем из БД информацию о последнем состоянии устройства
                String tmpIp = trassirChannelTmpl.getIp(); // считываем из БД информацию о ip устройства
                String tmpGuidIpDevice = trassirChannelTmpl.getGuidIpDevice();  // считываем из БД информацию о guid ip устройства
                String tmpModel = trassirChannelTmpl.getModel(); // считываем из БД информацию о модели устройства

                String tmpGuidChannel = trassirChannelTmpl.getGuidChannel(); // считываем из БД информацию о guid канала
                String tmpName = trassirChannelTmpl.getName(); // считываем из БД информацию о имени устройства
                String tmpGuidServer = trassirChannelTmpl.getGuidServer(); // считываем из БД guid сервера устройства

                // если нет сигнала, а в БД статус был ОК, то перезаписываем значение сигнала, все остальные не перезаписываем
                if ((trassirChannel.getSignal() == null
                        || trassirChannel.getSignal() == -1)
                        && trassirChannel.getSignal() != tmpSignal) {
                    trassirChannelService.updateByChannel(new TrassirChannelInfo(tmpGuidServer, tmpGuidChannel,
                            tmpName, trassirChannel.getSignal(), tmpGuidIpDevice, tmpIp, tmpModel, new Date()));
                } else { // иначе перезаписать все значения (если данные все данные от трассира были заполнены), кроме guid канала
                    if (trassirChannel.getGuidServer() != null
                            && trassirChannel.getGuidIpDevice() != null
                            && trassirChannel.getModel() != null
                            && trassirChannel.getIp() != null
                            && trassirChannel.getName() != null) {
                        trassirChannelService.updateByChannel(
                                new TrassirChannelInfo(trassirChannel.getGuidServer(), tmpGuidChannel,
                                        trassirChannel.getName(), trassirChannel.getSignal(),
                                        trassirChannel.getGuidIpDevice(), trassirChannel.getIp(),
                                        trassirChannel.getModel(), new Date()));

                    }
                }
            }
        }
        return channels; //todo удалить, если не нужно возвращать
    }
}