package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.service.SwitchService;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import com.backend.rebootingcameras.trassir_requests.*;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TrassirController {

    private List<TrassirChannelInfo> channelsFromTrassir; // список каналов (из запроса к Трассиру)
    private List<TrassirChannelInfo> channelsFromDB; // список (из БД)

    private List<TrassirServerInfo> servers; // список серверов (из запроса к Трассиру)
    private List<TrassirServerInfo> serversFromDB; // список (из БД)

    /* DI */
    private RestTemplate restTemplate; // DI для работы с запросами
    private TrassirChannelService trassirChannelService;
    private TrassirServerService trassirServerService;
    private SwitchService switchService;

    @Autowired
    public void setSwitchService(SwitchService switchService) {
        this.switchService = switchService;
    }

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
//    @Scheduled(initialDelay = 3000, fixedDelayString = "PT10S")
//    public void startCollectTrassirStats() {
//
//        System.out.println("Начало работы планировщика: " + new Date());
//
//        /* получение данных из БД */
//        serversFromDB = findAllServers();
//        channelsFromDB = findAllCameras();
//
//        fillServers(); // получаем данные из трассир
//
//        System.out.println("Данные по серверам обновлены в БД: " + new Date());
//
//        updateAllServersWithCheckingFields(servers); // обновляем данные серверов в БД
//
//        /* заполняем в цикле список каналов из Трассира */
//        channelsFromTrassir = new ArrayList<>();
//        if (servers != null) {
//            for (TrassirServerInfo server : servers) {
//                try {
//                    getChannels(server);
//                } catch (ResourceAccessException e) {
//                    server.setSessionId(null);
//                    System.out.println("Потеряна связь с сервером во время выполнения запроса к каналу\n" + e);
//                }
//            }
//        }
//        if (channelsFromDB == null && channelsFromTrassir != null) { // обновляем данные о каналах в БД, если БД пустая
//            updateAllChannels(channelsFromTrassir);
//        }
//
//        if (channelsFromDB != null && channelsFromTrassir != null) { // обновляем данные о каналах в БД, если БД полная
//            updateAllChannelsWithCheckingFields(channelsFromTrassir);
//        }
//        System.out.println("Данные по камерам обновлены в БД: " + new Date());
//    }


    /**
     * методы работы с каналами
     */

    /* получение списка каналов сервера из Трассира */
    private void getChannels(TrassirServerInfo server) throws ResourceAccessException {

        String serverIp = server.getServerIP();
        String SID = server.getSessionId();

        if (server.getSessionId() != null) { // выполняем, если до этого была получена сессия

            /* создаём url для получения guid канала */
            String urlForChannels = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                    PathForRequest.STRING_CHANNEL_LIST, SID); // для получения массива guid каналов

            /* получаем массив guid каналов сервера */
            TrassirGuid channelsGUIDs = restTemplate.getForObject(urlForChannels, TrassirGuid.class);

            System.out.println(urlForChannels);

            /* если массив guid был получен, иначе ничего не делаем и переходм к следующему серверу в цикле */
            if (channelsGUIDs != null) {

                /* получаем список каналов из Трассира */
                for (String guidChannel : channelsGUIDs.getSubdirs()) {

                    /* получаем имя канала */
                    String channelName = getChannelName(guidChannel, serverIp, SID);
                    System.out.println(channelName);

                    /* получаем статус канала */
                    Integer channelStatus = getChannelStatus(guidChannel, serverIp, SID);

                    //todo проверить какие поля можно получить при статусе -1
                    if (channelStatus == null || channelStatus == -1 || channelStatus == 0) { // если сигнала нет, то нет смысла выполнять последующие проверки
                        TrassirChannelInfo trassirChannel = new TrassirChannelInfo(server,
                                guidChannel, channelName,
                                -1,
                                null, null, null, new Date(), null, null, null, false);
                        channelsFromTrassir.add(trassirChannel);
                        System.out.println(trassirChannel);
                    } else {

                        /* получаем guid девайса */
                        String deviceGuidValue = getChannelGuid(guidChannel, serverIp, SID);

                        /* получаем ip девайса */
                        String deviceIpValue = getDeviceIp(serverIp, deviceGuidValue, SID);

                        /* получаем модель девайса */
                        String deviceModelValue = getDeviceModel(serverIp, deviceGuidValue, SID);

                        TrassirChannelInfo trassirChannel = new TrassirChannelInfo(server,
                                guidChannel, channelName,
                                channelStatus,
                                deviceGuidValue, deviceIpValue, deviceModelValue, new Date(), null, null, null, false);
                        channelsFromTrassir.add(trassirChannel);
                        System.out.println(trassirChannel);
                    }
                    threadSleepWithTryCatchBlock(30);
                }
            }
        }
    }

    /* получаем имя канала */
    private String getChannelName(String guidChannel, String serverIp, String SID) {

        String getChannelName = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_CHANNEL_NAME, SID);
        ChannelName channelName = restTemplate.getForObject(getChannelName, ChannelName.class);
        if (channelName == null || channelName.getError_code() != null) {
            channelName = new ChannelName();
            channelName.setValue("Неизвестное устройство");
        }
        threadSleepWithTryCatchBlock(30);
        return channelName.getValue();
    }

    /* получаем статус канала */
    private Integer getChannelStatus(String guidChannel, String serverIp, String SID) {
        String getChannelStatus = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_CHANNEL_FLAG_SIGNAL, SID);
        ChannelStatus channelStatus = restTemplate.getForObject(getChannelStatus, ChannelStatus.class);
        if (channelStatus == null || channelStatus.getError_code() != null) {
            channelStatus = new ChannelStatus();
            channelStatus.setValue(null);
        }
        threadSleepWithTryCatchBlock(30);
        return channelStatus.getValue();
    }

    /* получаем guid девайса */
    private String getChannelGuid(String guidChannel, String serverIp, String SID) {
        String deviceGuidValue;
        String getDeviceGuid = String.format(PathForRequest.STRING_FOR_FORMAT, serverIp,
                PathForRequest.STRING_CHANNEL_LIST + guidChannel + PathForRequest.STRING_DEVICE_GUID, SID);
        DeviceGuid deviceGuid = restTemplate.getForObject(getDeviceGuid, DeviceGuid.class);
        if (deviceGuid == null || deviceGuid.getError_code() != null) {
            threadSleepWithTryCatchBlock(30);
            return null;
        } else {
            StringBuilder stringBuffer = new StringBuilder(deviceGuid.getValue());
            deviceGuidValue = stringBuffer.delete(0, 21).toString(); // удаляем лишние элементы из строки
            threadSleepWithTryCatchBlock(30);
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

            threadSleepWithTryCatchBlock(30);
            return deviceIpValue;

        } else {
            threadSleepWithTryCatchBlock(30);
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

            threadSleepWithTryCatchBlock(30);
            return deviceModelValue;

        } else {
            threadSleepWithTryCatchBlock(20);
            return null;
        }
    }

    /**
     * методы работы с БД каналов
     */

    /* записываем в БД список камер */ //todo убрать возвращаемы тип, если нигде не понадобится
    List<TrassirChannelInfo> updateAllChannels(List<TrassirChannelInfo> channels) {
        return trassirChannelService.updateAll(channels);
    }

    /* получаем список каналов из БД */
    List<TrassirChannelInfo> findAllCameras() {
        return trassirChannelService.findAll();
    }

    /* обновление всех данных с проверкой параметров */ //todo убрать возвращаемый тип, если нигде не понадобится
    private List<TrassirChannelInfo> updateAllChannelsWithCheckingFields(List<TrassirChannelInfo> channels) {

        /* проверка для каждого канала из БД, что guid канала есть в переданном массиве каналов из Трассира */
        for (TrassirChannelInfo channelFromDB : channelsFromDB) {
            boolean isHas = channels.stream().map(ch -> ch.getGuidChannel())
                    .anyMatch(guid -> guid.equals(channelFromDB.getGuidChannel()));
            /* если guid не найден, значит меняем статус канала в БД на -1 */
            if (!isHas) {
                channelFromDB.setSignal(-1);
                trassirChannelService.updateByChannel(channelFromDB);
            }
        }
        /* проверяем все полученные каналы от трассира */
        for (TrassirChannelInfo trassirChannel : channels) {

            // получаем из БД в цикле все камеры
            TrassirChannelInfo trassirChannelFromDB = trassirChannelService.findByGuid(trassirChannel.getGuidChannel());

            if (trassirChannelFromDB == null) { // если элемент в БД не найден, значит это новая запись (камера)
                trassirChannelService.updateByChannel(trassirChannel); // добавляем её в БД
            } else {
//                Integer tmpSignal = trassirChannelFromDB.getSignal(); // считываем из БД информацию о последнем состоянии устройства
                String tmpIp = trassirChannelFromDB.getIp(); // считываем из БД информацию о ip устройства
                String tmpGuidIpDevice = trassirChannelFromDB.getGuidIpDevice();  // считываем из БД информацию о guid ip устройства
                String tmpModel = trassirChannelFromDB.getModel(); // считываем из БД информацию о модели устройства

                String tmpGuidChannel = trassirChannelFromDB.getGuidChannel(); // считываем из БД информацию о guid канала
                String tmpName = trassirChannelFromDB.getName(); // считываем из БД информацию о имени устройства
                TrassirServerInfo tmpServer = trassirChannelFromDB.getGuidServer(); // считываем из БД сервер устройства
                Boolean tmpPoeInjector = trassirChannelFromDB.getPoeInjector(); // считываем из БД информацию о poe инжекторе
                Switch tmpSwitch = trassirChannelFromDB.getSwitchId(); // считываем из БД информацию о коммутаторе
                Integer tmpPort = trassirChannelFromDB.getPort(); // считываем из БД информацию о порте коммутатора
                Date lastUpdate = trassirChannelFromDB.getLustUpdate(); // считываем из БД информацию о последнем изменении состояния
                Boolean tmpLostChannel = trassirChannelFromDB.getLostChannel(); // потерянный//не потерянный канал

                trassirChannelService.updateByChannel(
                        new TrassirChannelInfo(trassirChannel.getGuidServer() != null ? trassirChannel.getGuidServer() : tmpServer,
                                trassirChannel.getGuidChannel() != null ? trassirChannel.getGuidChannel() : tmpGuidChannel,
                                trassirChannel.getName() != null ? trassirChannel.getName() : tmpName,
                                trassirChannel.getSignal() != null ? trassirChannel.getSignal() : -1,
                                trassirChannel.getGuidIpDevice() != null ? trassirChannel.getGuidIpDevice() : tmpGuidIpDevice,
                                trassirChannel.getIp() != null ? trassirChannel.getIp() : tmpIp,
                                trassirChannel.getModel() != null ? trassirChannel.getModel() : tmpModel,
                                trassirChannel.getSignal() == null
                                        || trassirChannel.getSignal() == 0
                                        || trassirChannel.getSignal() == -1 ? lastUpdate : new Date(),
                                tmpPoeInjector, tmpSwitch, tmpPort, tmpLostChannel));
            }
        }
        return channels; //todo удалить, если не нужно возвращать
    }

    /**
     * заполнение информации о серверах
     */
    private void fillServers() {

        servers = serversFromDB;

        for (TrassirServerInfo server : servers) {
            TrassirSession session;
            //todo подумать над реализацией, т.к. в данный момент сессия сохранена на 13 минут в БД, и если связь теряется в этом промежутке,
            // то проверка try-catch уже не срабатывает (пока только на ум приходит получение сессии каждый раз)
            if (!checkLastSessionUpdate(server)) {
                System.out.println("Время сессии " + server.getServerName() + " истекло, нужно обновить");
                System.out.println("-----------------------------------------");
                try {
                    session = getSession(server);
                } catch (ResourceAccessException e) {
                    server.setServerStatus(-1); //todo посмотреть какой статус лучше подходит (null или -1)
                    session = null;
                    System.out.println("Поймана ошибка! " + e + " Нет соединения с сервером");
                }
            } else {
                System.out.println("Обновление сессии " + server.getServerName() + " не требуется");
                System.out.println("---------------------------------------");
                session = new TrassirSession(server.getSessionId(), null, null);
                server.setLustUpdate(new Date());
            }

            /* заполняю данные состояния */
            if (session != null && session.getSid() != null && session.getError_code() == null) { // если получена сессия
                String sessionId = session.getSid();
                server.setSessionId(sessionId); // сохраняю значение полученной сессии

                // получаю состояние сервера
                String getServerHealth = String.format(PathForRequest.STRING_FOR_FORMAT, server.getServerIP(),
                        PathForRequest.STRING_SERVER_HEALTH, sessionId);
                ServerHealth serverHealth = restTemplate.getForObject(getServerHealth, ServerHealth.class);

                /* записываю полученные данные, если пришёл ответ */
                if (serverHealth != null && serverHealth.getError_code() == null) {
                    server.setChannelsTotal(serverHealth.getChannels_total());
                    server.setChannelsOnline(serverHealth.getChannels_online());
                    server.setServerStatus(serverHealth.getNetwork());
                }
                /* блок для записи отсутсвия сигнала от сервера, если время сессии в БД ещё не истекло */
                else {
                    server.setSessionId(null);
                    server.setChannelsTotal(null);
                    server.setChannelsOnline(null);
                    server.setServerStatus(-1);
                }
                /* усыпляем поток перед следующим вызовом */
                threadSleepWithTryCatchBlock(30);

                /* заполняю имя сервера */
                String getServerName = String.format(PathForRequest.STRING_FOR_FORMAT, server.getServerIP(),
                        PathForRequest.STRING_SERVER_NAME, sessionId);
                ServerName serverName = restTemplate.getForObject(getServerName, ServerName.class);

                if (serverName != null && serverName.getError_code() == null) {
                    server.setServerName(serverName.getValue());
                }

            } else { // если сессия не была получена
                server.setSessionId(null);
                server.setChannelsTotal(null);
                server.setChannelsOnline(null);
                server.setServerStatus(-1); //todo проверить, какое значение означает отсутствие связи с сервером
            }

            System.out.println("ID session:");
            System.out.println(server.getSessionId());

            /* усыпляем поток перед следующим вызовом */
            threadSleepWithTryCatchBlock(30);
        }
    }

    /**
     * методы работы с БД серверов
     */

    /* получаем список серверов из БД */
    List<TrassirServerInfo> findAllServers() {
        return trassirServerService.findAll();
    }

    /* записываем в БД список серверов */
    List<TrassirServerInfo> updateAllServers(List<TrassirServerInfo> servers) {
        return trassirServerService.updateAll(servers);
    }

    /* обновление всех данных с проверкой параметров */ //todo убрать возвращаемые типы, если нигде не понадобится
    List<TrassirServerInfo> updateAllServersWithCheckingFields(List<TrassirServerInfo> servers) {
        if (servers == null) {
            return null;
        } else {
            for (TrassirServerInfo server : servers) {

                // без проверки на добавление нового сервера, т.к. мы сами добавляем новый сервер вручную в БД
                // (его guid и ip). В будущем можно сделать метод по проверке новых серверов по guid (если добавляется в клиенте)
                TrassirServerInfo serverFromDb = trassirServerService.findByGuid(server.getGuid());

                if (!serverFromDb.equals(server)) {
                    trassirServerService.updateByServer(new TrassirServerInfo(
                            server.getGuid() != null ? server.getGuid() : serverFromDb.getGuid(),
                            server.getServerName() != null ? server.getServerName() : serverFromDb.getServerName(),
                            serverFromDb.getServerIP(),
                            server.getChannelsTotal() != null ? server.getChannelsTotal()  : serverFromDb.getChannelsTotal(),
                            server.getChannelsOnline(), server.getServerStatus(),
                            server.getSessionId(), new Date(), server.getErrorCode()));
                }
            }
        }
        return servers;
    }

    /**
     * утилиты
     */

    /* вынесенное в отдельный метод усыпление потока перед новым запросом в Трассир */
    private void threadSleepWithTryCatchBlock(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* проверка, живая ли сессия: true - значит жива */
    public boolean checkLastSessionUpdate(TrassirServerInfo server) {
        if (server == null || server.getLustUpdate() == null || server.getSessionId() == null) {
            return false;
        } else {
            Calendar calendarNewTime = Calendar.getInstance();
            calendarNewTime.setTime(new Date());
            Calendar calendarLastUpdateTime = Calendar.getInstance();
            calendarLastUpdateTime.setTime(server.getLustUpdate());

            long result = calendarNewTime.getTimeInMillis() - calendarLastUpdateTime.getTimeInMillis();
            if (result > PathForRequest.TIME_BETWEEN_NOW_AND_GET_SESSION) {
                return false;
            } else {
                return true;
            }
        }
    }

    /* получение сессии */
    public TrassirSession getSession(TrassirServerInfo server) throws ResourceAccessException {

        String getSessionUrl = String.format(PathForRequest.STRING_FOR_SESSION, server.getServerIP());  // строка для получения сессии

        TrassirSession session = restTemplate.getForObject(getSessionUrl, TrassirSession.class);

        if (session != null) {
            server.setSessionId(session.getSid());
            server.setLustUpdate(new Date()); // время последнего обновления
        }

        return session;
    }

}