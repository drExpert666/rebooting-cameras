package com.backend.rebootingcameras.utils;


import com.backend.rebootingcameras.models.ServersGuids;
import com.backend.rebootingcameras.models.UserCommonRights;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/test01")
public class Test01 {

    private TrassirChannelService channelService;

    @Autowired
    public void setChannelService(TrassirChannelService channelService) {
        this.channelService = channelService;
    }

    public final static String RECEIVED_VALUES =
"/O1GNrsQJ,4462471020544,/O1GNrsQJ/channels,1,/O1GNrsQJ/channels/DIik7J9R,2199023255552,/O1GNrsQJ/channels/Dcnw0N33,2199023255552,/O1GNrsQJ/channels/EMQftfRs,2,/O1GNrsQJ/channels/GSLmqKA5,2199023255552,/O1GNrsQJ/channels/RtOmwM4K,2,/O1GNrsQJ/channels/UN1asQSo,2199023255552,/O1GNrsQJ/channels/XRubR01G,2203318222848,/O1GNrsQJ/channels/d6bfDs4w,1038,/O1GNrsQJ/channels/nVwEerNV,1024,/O1GNrsQJ/channels/stIBTSpu,2,/O1GNrsQJ/channels/wTk0FjzS,2203318222848,/O1GNrsQJ/channels/zyaR6Ukv,2203318222848,/gZZKuo60,4458176053248,/gZZKuo60/channels,2203318222848,/h0ZCZ2Yf,4458176053248,/hqpstp9l,4462471020544,/kKAZPSPI/channels/pAEDZSxC,6661494276096,/kKAZPSPI/channels/vc2h25f1,6661494276096,/kKAZPSPI/channels/vuSCsFQY,6661494276096,/pqzJwZy9,4458176053248,/zLuaHPw1/persons,21474836480,/zLuaHPw1/templates,21474836480";
    /* базовые значения */
    static HashMap<Integer, String> baseAddedRights = new HashMap<>();
    static HashMap<Integer, String> baseDeletedRights = new HashMap<>();
    static HashMap<Integer, String> baseUserRights = new HashMap<>();

    /* добавленные/удалённые значения */
    static HashMap<String, Long> optionalAddedRights = new HashMap<>();
    static HashMap<String, Long> optionalDeletedRights = new HashMap<>();

    static List<Integer> receivedRights = new ArrayList<>();


    @GetMapping("/channels")
    public void main() {

        Test01 test01 = new Test01();

        //todo поменять реализацию после подключения БД
        /* получили и заполнили хэш-мэпы с базовыми правами */
        UserCommonRights userCommonRights = new UserCommonRights();
        baseAddedRights = userCommonRights.fillAddedRights();
        baseDeletedRights = userCommonRights.fillDeletedRights();

//        long a1 = 12884901888L;
        long a1 = 1903L;
        /* получили базовые права пользователя */
        receivedRights = getRights(a1);
        /* кладём права в хэшмэп со значениями */
        for (Integer right : receivedRights) {
            baseUserRights.put(right, baseAddedRights.get(right));
        }
        System.out.println("Базовые права пользователя: ");
        System.out.println(baseUserRights);

        /* парсим полученную строки по запросу к acl */
        List<String> substr = parseRights(RECEIVED_VALUES);

        /* заполняю дополнительные права пользователя */
        fillUserOptionalRights(substr);
        System.out.println("---------------------------------------------------------");
        System.out.println("На добавление права пользователя: ");
        optionalAddedRights.forEach((k, v) -> System.out.println(k + " " + v));
        System.out.println("---------------------------------------------------------");
        System.out.println("На удаление права пользователя: ");
        optionalDeletedRights.forEach((k, v) -> System.out.println(k + " " + v));

        ServersGuids serversGuids = new ServersGuids();

        /* оставшиеся сервера у юзера с галочкой "просмотр" на всё сервере */
        List<String> serversLeft = fillUserServers(serversGuids.serversGuidList);
        System.out.println("---------------------------------------------------------");
        System.out.println("Получаю список оставшихся в полном доступе (на просмотр) серверов ");
        System.out.println(serversLeft);
        List<String> serversLeftAfterUpdate = checkServersWithAllRights(serversLeft);
        System.out.println("Получаю список оставшихся серверов после доп проверки каналов");
        System.out.println(serversLeftAfterUpdate);

        //todo тут получить список всех камер из БД по оставшимся серверам
        System.out.println("---------------------------------------------------------");
        System.out.println("Получаю список камер на удаление: ");
        System.out.println(deleteUserChannels());
        List<String> channelsForDelete = deleteUserChannels();
        List<TrassirChannelInfo> channelsFromDB = new ArrayList<>();
        for (String s : serversLeftAfterUpdate) {
            channelsFromDB.addAll(getChannelsByParams(s));
        }
        for (TrassirChannelInfo channel :
                channelsFromDB) {
            System.out.println(channel.getName() + " " + channel.getGuidChannel());
        }
        System.out.println("---------------------------");
        System.out.println("Общее количество камер: " + channelsFromDB.size());

        /* удаляю камеры по guid из списка полученного в поле acl */
        List<TrassirChannelInfo> channelsToDelete = new ArrayList<>();
        for (String channelGuid : channelsForDelete) {
            channelGuid = new StringBuilder(channelGuid).delete(0, 18).toString();
            String finalChannelGuid = channelGuid;
            System.out.println(finalChannelGuid);
            channelsFromDB.forEach(ch -> {
                if (ch.getGuidChannel().equals(finalChannelGuid)) {
                    System.out.println("---------------------------");
                    System.out.println("Удаляю: " + finalChannelGuid);
                    channelsToDelete.add(ch);
                }
            });
        }
//        List<TrassirChannelInfo> channelsLeft = new ArrayList<>();
        channelsFromDB.removeAll(channelsToDelete);

        System.out.println("---------------------------");
        System.out.println("Общее количество камер после удаления: " + channelsFromDB.size());

        /* добавляю камеры в список камер юзера */
        List<String> channelsGuid = new ArrayList<>();
        optionalAddedRights.forEach((k,v) -> {
            List<Integer> rightsForAdd = getRights(v);
            if (rightsForAdd.contains(0) && k.contains("/channels") && k.length() > 17) {
                StringBuilder builder = new StringBuilder(k);
                String res = builder.delete(0, 18).toString();
                channelsGuid.add(res);
            }
        });

        System.out.println("---------------------------");
        System.out.println("Список камер для добавления: " + channelsGuid);

        List channelsForAdd = getChannelsByChannelGuid(channelsGuid);
        channelsFromDB.addAll(channelsForAdd);
        System.out.println("---------------------------");
        System.out.println("Общее количество камер после добавления: " + channelsFromDB.size());


    }

    /* получаем список базовых прав юзера */
    private static List<Integer> getRights(long rights) {
        List<Integer> receivedRights = new ArrayList<>();
        String result = Long.toBinaryString(rights);
        StringBuilder builder = new StringBuilder(result);
        result = builder.reverse().toString();
        int bitMask = result.indexOf('1');
        while (bitMask >= 0) {
            receivedRights.add(bitMask);
            bitMask = result.indexOf('1', bitMask + 1);
        }
        return receivedRights;
    }

    /* парсим полученную строку из acl */
    private static List<String> parseRights(String value) {
        if (value != null && !value.equals("")) {
            StringBuilder builder = new StringBuilder(value);
            value = builder.delete(0, 1).toString();

            List<String> list =
                    Stream.of(value.split(",/"))
                            .collect(Collectors.toList());
            return list;
        } else
            return null;
    }

    /* заполняем права (добавленные и удалённые) */
    private static void fillUserOptionalRights(List<String> userOptionalRights) {
        userOptionalRights.stream().forEach(r -> {
            String key = r.split(",")[0];
            Long value = Long.valueOf(r.split(",")[1]);
            if (value != null && value < 2000) {
                optionalAddedRights.put(key, value);
            } else {
                optionalDeletedRights.put(key, value);
            }
        });
    }

    /* заполняем список доступных серверов у юзера с галочкой на просмотр у всего сервера*/
    private static List<String> fillUserServers(List<String> serversGuids) {
        System.out.println("-------------------------------");
        System.out.println("Всего серверов в доступе: " + serversGuids);
        if (baseUserRights.get(0) != null) {
            optionalDeletedRights.forEach((k, v) -> {
                /* если в ключе указан только гуид сервера */
                if (k.length() == 8) {
                    System.out.println("-------------------------------");
                    System.out.println(getRights(v));
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        System.out.println("Удаляю сервер из доступа (указан только гуид сервера): " + k);
                        serversGuids.remove(k);
                        System.out.println("Осталось серверов: ");
                        System.out.println(serversGuids);
                    }
                }
                /* если в ключе указан только гуид канала () */
                if (k.length() == 17) {
                    System.out.println("-------------------------------");
                    System.out.println(getRights(v));
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        StringBuilder builder = new StringBuilder(k);
                        k = builder.delete(8, 17).toString();

                        System.out.println("Удаляю сервер из доступа (указана только общая строка канал): " + k);
                        serversGuids.remove(k);
                        System.out.println("Осталось серверов: ");
                        System.out.println(serversGuids);
                    }
                }

            });
        }
        return serversGuids;
    }

    /* заполняем список камер на удаление */
    private static List<String> deleteUserChannels() {
        List<String> channelsForRemove = new ArrayList<>();
        if (baseUserRights.get(0) != null) {
            optionalDeletedRights.forEach((k, v) -> {
                if (k.contains("/channels") && k.length() > 17) {
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        channelsForRemove.add(k);
                    }
                }
            });
        }
        return channelsForRemove;
    }

    /* делаю запрос к БД на получение всех доступных камер с полным доступом на просмотр у сервера */
    private static List<String> checkServersWithAllRights(List<String> serversUserGuid) {
        /* проверяю, есть ли в правах на добавление поле с указанием всех каналов на просмотр (формат i8MTdNGd/channels) */
        optionalAddedRights.forEach((k, v) -> {
            /* если в ключе указан только гуид канала () */
            if (k.length() == 17) {
                System.out.println("-------------------------------");
                List<Integer> rightsForAdd = getRights(v);
                if (rightsForAdd.contains(0)) {
                    StringBuilder builder = new StringBuilder(k);
                    k = builder.delete(8, 17).toString();

                    System.out.println("Добавляю сервер в доступ (указана только общая строка канал): " + k);
                    serversUserGuid.add(k);
                    System.out.println("Осталось серверов: ");
                    System.out.println(serversUserGuid);
                }
            }
        });
        return serversUserGuid;
    }


    /* ищу каналы по гуид сервера */
    private List<TrassirChannelInfo> getChannelsByParams(String serv) {
        List<TrassirChannelInfo> channels = channelService.findByParams(serv, null, null, null,
                null, null, null, null).getContent();
        return channels;
    }

    private List<TrassirChannelInfo> getChannelsByChannelGuid(List<String> channelGuids) {
        List<TrassirChannelInfo> channelsForAdd = new ArrayList<>();
        for (String channelGuid: channelGuids) {
           channelsForAdd.add(channelService.findByGuid(channelGuid));
        }
        return channelsForAdd;
    }

}
