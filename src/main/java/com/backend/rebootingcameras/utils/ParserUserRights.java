package com.backend.rebootingcameras.utils;


import com.backend.rebootingcameras.models.ServersGuids;
import com.backend.rebootingcameras.models.UserCommonRights;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ParserUserRights {

    private TrassirChannelService channelService;
    @Autowired
    public void setChannelService(TrassirChannelService channelService) {
        this.channelService = channelService;
    }

    /* базовые значения */
    static HashMap<Integer, String> baseAddedRights = new HashMap<>();
    static HashMap<Integer, String> baseDeletedRights = new HashMap<>();

    private HashMap<Integer, String> baseUserRights = new HashMap<>();

    /* добавленные/удалённые значения */
    private HashMap<String, Long> optionalAddedRights = new HashMap<>();
    private HashMap<String, Long> optionalDeletedRights = new HashMap<>();

    private List<Integer> receivedRights = new ArrayList<>();


    public String getChannelsFromUserTrassir(Integer baseRights, String aclRights) {

        //todo поменять реализацию после подключения БД
        /* получили и заполнили хэш-мэпы с базовыми правами */
        UserCommonRights userCommonRights = new UserCommonRights();
        baseAddedRights = userCommonRights.fillAddedRights();
        baseDeletedRights = userCommonRights.fillDeletedRights();

        /* получили базовые права пользователя */
        receivedRights = getRights(baseRights);

        /* кладём права в хэшмэп со значениями */
        baseUserRights = new HashMap<>();
            for (Integer right : receivedRights) {
                baseUserRights.put(right, baseAddedRights.get(right));
            }

        /* парсим полученную строки по запросу к acl */
        List<String> substr = parseRights(aclRights);

        /* заполняю дополнительные права пользователя */
        fillUserOptionalRights(substr);
        ServersGuids serversGuids = new ServersGuids();

        List<String> serversLeftAfterUpdate;
        List<String> serversLeft;
        if (baseRights == 0) {
            /* оставшиеся сервера у юзера с галочкой "просмотр" на всё сервере */
           serversLeft = fillUserServersByAdding();

          serversLeftAfterUpdate = checkServersWithAllRightsOnDelete(serversLeft);
        }

        else {
            /* оставшиеся сервера у юзера с галочкой "просмотр" на всё сервере */
           serversLeft = fillUserServersByDeleting(serversGuids.serversGuidList);
           serversLeftAfterUpdate = checkServersWithAllRightsOnAdd(serversLeft);
        }

        //todo тут получить список всех камер из БД по оставшимся серверам
        List<String> channelsForDelete = deleteUserChannels();
        List<TrassirChannelInfo> channelsFromDB = new ArrayList<>();
        for (String s : serversLeftAfterUpdate) {
            channelsFromDB.addAll(getChannelsByParams(s));
        }

        /* удаляю камеры по guid из списка полученного в поле acl */
        List<TrassirChannelInfo> channelsToDelete = new ArrayList<>();
        for (String channelGuid : channelsForDelete) {
            channelGuid = new StringBuilder(channelGuid).delete(0, 18).toString();
            String finalChannelGuid = channelGuid;
            channelsFromDB.forEach(ch -> {
                if (ch.getGuidChannel().equals(finalChannelGuid)) {
                    channelsToDelete.add(ch);
                }
            });
        }
        channelsFromDB.removeAll(channelsToDelete);

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

        List channelsForAdd = getChannelsByChannelGuid(channelsGuid);
        channelsFromDB.addAll(channelsForAdd);

        StringBuilder builder = new StringBuilder();
        channelsFromDB.forEach(ch -> {
            if (ch != null) {
               builder.append(ch.getGuidChannel()).append(",");
            }

        });
        return builder.toString();
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
    private void fillUserOptionalRights(List<String> userOptionalRights) {
        optionalAddedRights = new HashMap<>();
        optionalDeletedRights = new HashMap<>();
        if (userOptionalRights != null) {
            userOptionalRights.forEach(r -> {
                String key = r.split(",")[0];
                Long value = Long.valueOf(r.split(",")[1]);
                if (value != null && value < 2000) {
                    optionalAddedRights.put(key, value);
                } else {
                    optionalDeletedRights.put(key, value);
                }
            });
        }
    }

    /* заполняем список доступных серверов у юзера с галочкой на просмотр у всего сервера*/
    private List<String> fillUserServersByDeleting(List<String> serversGuids) {
        if (baseUserRights.get(0) != null) {
            optionalDeletedRights.forEach((k, v) -> {
                /* если в ключе указан только гуид сервера */
                if (k.length() == 8) {
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        serversGuids.remove(k);
                    }
                }
                /* если в ключе указан только гуид канала () */
                if (k.length() == 17) {
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        StringBuilder builder = new StringBuilder(k);
                        k = builder.delete(8, 17).toString();
                        serversGuids.remove(k);
                    }
                }
            });
        }
        return serversGuids;
    }

    /* заполняем список доступных серверов у юзера с галочкой на просмотр у всего сервера*/
    private List<String> fillUserServersByAdding() {
        List<String> serversGuids = new ArrayList<>();
            optionalAddedRights.forEach((k, v) -> {
                /* если в ключе указан только гуид сервера */
                if (k.length() == 8) {
                    List<Integer> rightsForAdd = getRights(v);
                    if (rightsForAdd.contains(0)) {
                        serversGuids.add(k);
                    }
                }
                /* если в ключе указан только гуид канала () */
                if (k.length() == 17) {
                    List<Integer> rightsForAdd = getRights(v);
                    if (rightsForAdd.contains(0)) {
                        StringBuilder builder = new StringBuilder(k);
                        k = builder.delete(8, 17).toString();
                        serversGuids.add(k);
                    }
                }
            });
        return serversGuids;
    }

    /* заполняем список камер на удаление */
    private List<String> deleteUserChannels() {
        List<String> channelsForRemove = new ArrayList<>();
            optionalDeletedRights.forEach((k, v) -> {
                if (k.contains("/channels") && k.length() > 17) {
                    List<Integer> rightsForRemove = getRights(v);
                    if (rightsForRemove.contains(32)) {
                        channelsForRemove.add(k);
                    }
                }
            });
        return channelsForRemove;
    }

    /* делаю запрос к БД на получение всех доступных камер с полным доступом на просмотр у сервера */
    private List<String> checkServersWithAllRightsOnAdd(List<String> serversUserGuid) {
        /* проверяю, есть ли в правах на добавление поле с указанием всех каналов на просмотр (формат i8MTdNGd/channels) */
        optionalAddedRights.forEach((k, v) -> {
            /* если в ключе указан только гуид канала () */
            if (k.length() == 17) {
                List<Integer> rightsForAdd = getRights(v);
                if (rightsForAdd.contains(0)) {
                    StringBuilder builder = new StringBuilder(k);
                    k = builder.delete(8, 17).toString();
                    serversUserGuid.add(k);
                }
            }
        });
        return serversUserGuid;
    }

    /* делаю запрос к БД на получение всех доступных камер с полным доступом на просмотр у сервера если базовые права были 0*/
    private List<String> checkServersWithAllRightsOnDelete(List<String> serversLeft) {
        /* проверяю, есть ли в правах на добавление поле с указанием всех каналов на просмотр (формат i8MTdNGd/channels) */
        optionalDeletedRights.forEach((k, v) -> {
            /* если в ключе указан только гуид канала () */
            if (k.length() == 17) {
                List<Integer> rightsForAdd = getRights(v);
                if (rightsForAdd.contains(32)) {
                    StringBuilder builder = new StringBuilder(k);
                    k = builder.delete(8, 17).toString();
                    serversLeft.remove(k);
                }
            }
        });
        return serversLeft;
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
