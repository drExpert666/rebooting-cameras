package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.service.UserRightsService;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import com.backend.rebootingcameras.trassir_models.TrassirUserRightsInfo;
import com.backend.rebootingcameras.trassir_requests.*;
import com.backend.rebootingcameras.utils.ParserUserRights;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserRightsController {

    /* DI */
    private RestTemplate restTemplate; // DI для работы с запросами
    private TrassirChannelService trassirChannelService;
    private TrassirServerService trassirServerService;
    private TrassirController trassirController;
    private UserRightsService userRightsService;
    private ParserUserRights parserUserRights;

    @Autowired
    public void setParserUserRights(ParserUserRights parserUserRights) {
        this.parserUserRights = parserUserRights;
    }

    @Autowired
    public void setUserRightsService(UserRightsService userRightsService) {
        this.userRightsService = userRightsService;
    }

    @Autowired
    public void setTrassirController(TrassirController trassirController) {
        this.trassirController = trassirController;
    }

    @Autowired
    public void setTrassirServerService(TrassirServerService trassirServerService) {
        this.trassirServerService = trassirServerService;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setTrassirChannelService(TrassirChannelService trassirChannelService) {
        this.trassirChannelService = trassirChannelService;
    }


    @Scheduled(initialDelay = 1000, fixedDelayString = "PT5S")
    public void startCollectUserRights() {
        /* записываю данные о сервере из БД в переменную */
        String guidServer = "gZZKuo60";//todo получить guid сервера из БД
        TrassirServerInfo trassirServer = trassirServerService.findByGuid(guidServer);

        /* получаю айди пользователей из трассира */
        List<String> usersGuid = fillUserRights(trassirServer);

        List<TrassirUserRightsInfo> userRights = new ArrayList<>();

        /* заполняю данные о каждом пользователе из трассира */
        if (usersGuid != null) {
            for (String userId : usersGuid) {
                /* получаю базовые права пользователя */
                Integer baseRights = getBaseUserRights(userId, trassirServer);
                /* получаю дополнительные права пользователя */
                String aclRights = getAclUserRights(userId, trassirServer);
                /* получаю тип пользователя */
                String userType = getUserType(userId, trassirServer);
                /* получаю группу пользователя */
                String userGroup = null;
                if (userType.equals("User")) {
                    userGroup = getUserGroup(userId, trassirServer);
                }
                /* получаю имя пользователя */
                String userName = getUserName(userId, trassirServer);
                if (userId != null && baseRights != null && aclRights != null) {
                    String userChannels = userRightsService.findById(userId).getChannels();
                    TrassirUserRightsInfo trassirUserRights =
                            new TrassirUserRightsInfo(userId, userName, baseRights, aclRights,
                                    (userChannels != null && userChannels.trim().length() > 0) ? userChannels : null,
//                                    null,
                                    trassirServer.getGuid(), userType, userGroup);
                    userRights.add(trassirUserRights);
                } else {
                    System.out.println("Ошибка, гуид пользователя: " + userId + " " + userName);
                    System.out.println("Ошибка, baseRights пользователя: " + baseRights);
                    System.out.println("Ошибка, aclRights пользователя: " + aclRights);
                }
            }
        }
        List<TrassirUserRightsInfo> trassirUserRightsInfos = userRightsService.saveAll(userRights);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* добавляю каналы */
        List<TrassirUserRightsInfo> trassirUsersForUpdate = new ArrayList<>();
        trassirUserRightsInfos.forEach(ur -> {
            /* если группа не указана */
            if (ur.getGroupId() == null) {
                String channels = parserUserRights.getChannelsFromUserTrassir(ur.getBaseRights(), ur.getAcl());
                ur.setChannels(channels);
                /* записываю список каналов в БД */
                //userRightsService.update(userRightsInfo);
                trassirUsersForUpdate.add(ur);
            /* если группа указана */
            } else {
                List<TrassirUserRightsInfo> usersRightsInfo = userRightsService.findByGroupGuid(ur.getGroupId());
                TrassirUserRightsInfo userGroup = userRightsService.findById(ur.getGroupId());
                for (TrassirUserRightsInfo userRightsInfo : usersRightsInfo) {
                    String channels = userGroup.getChannels();
                    if (userRightsInfo.getAcl() == null || userRightsInfo.getAcl().trim().length() == 0) {
                        userRightsInfo.setChannels(channels);
                    } else {
                        String userBaseAcl = userGroup.getAcl();
                        Integer userBaseRights = userGroup.getBaseRights();
                        String aclRights;
                        /* проверяю, пуст ли ACL для группы (чтобы не передавать запятую для парсинга) */
                        if (userGroup.getAcl() == null || userGroup.getAcl().trim().length() == 0) {
                            aclRights = userRightsInfo.getAcl();
                        } else {
                            aclRights = userBaseAcl + "," + userRightsInfo.getAcl();
                        }
                        String userChannels = parserUserRights.getChannelsFromUserTrassir(userBaseRights, aclRights);
                        userRightsInfo.setChannels(userChannels);
                    }
                    trassirUsersForUpdate.add(userRightsInfo);
                }

            }
        });
        userRightsService.saveAll(trassirUsersForUpdate);
    }


    /* получаю айди пользователей из трассира */
    private List<String> fillUserRights(TrassirServerInfo trassirServer) {
        List<String> usersGuid;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(), PathForRequest.STRING_USER_LIST, trassirServer.getSessionId());
            UserList userList = restTemplate.getForObject(url, UserList.class);
            if (userList != null && userList.getError_code() == null && userList.getSubdirs() != null) {
                usersGuid = Arrays.asList(userList.getSubdirs());
            } else {
                usersGuid = null;
            }
        } else {
            TrassirSession session = null;
            try {
                session = trassirController.getSession(trassirServer);
            } catch (Exception e) {
                System.out.println("Поймана ошибка: " + e);
            }
            if (session != null) {
                trassirServer.setSessionId(session.getSid());
                trassirServer.setLustUpdate(new Date());
                trassirServerService.updateByServer(trassirServer);
            }
            usersGuid = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return usersGuid;
    }

    /* получаю базовые права пользователя */
    private Integer getBaseUserRights(String guid, TrassirServerInfo trassirServer) {
        Integer baseRights;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(),
                    PathForRequest.STRING_USER_LIST + guid + "/" + PathForRequest.STRING_BASE_RIGHTS,
                    trassirServer.getSessionId());
            UserRightsBaseRights userRightsBaseRights = restTemplate.getForObject(url, UserRightsBaseRights.class);
            if (userRightsBaseRights != null && userRightsBaseRights.getError_code() == null && userRightsBaseRights.getValue() != null) {
                baseRights = userRightsBaseRights.getValue();
            } else {
                baseRights = null;
            }
        } else {
            baseRights = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return baseRights;  //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
    }

    /* получаю дополнительные права пользователя */
    private String getAclUserRights(String guid, TrassirServerInfo trassirServer) {
        String aclRights;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(),
                    PathForRequest.STRING_USER_LIST + guid + "/" + PathForRequest.STRING_ACL_RIGHTS,
                    trassirServer.getSessionId());
            UserRightsAcl userRightsAcl = restTemplate.getForObject(url, UserRightsAcl.class);
            if (userRightsAcl != null && userRightsAcl.getError_code() == null && userRightsAcl.getValue() != null) {
                aclRights = userRightsAcl.getValue();
            } else {
                aclRights = null;
            }
        } else {
            aclRights = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return aclRights;  //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
    }

    /* получаю имя пользователя */
    private String getUserName(String guid, TrassirServerInfo trassirServer) {
        String name;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(),
                    PathForRequest.STRING_USER_LIST + guid + "/" + "name",
                    trassirServer.getSessionId());
            UserName userName = restTemplate.getForObject(url, UserName.class);
            if (userName != null && userName.getError_code() == null && userName.getValue() != null) {
                name = userName.getValue();
            } else {
                name = null;
            }
        } else {
            name = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name;  //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
    }

    /* получаю тип пользователя */
    private String getUserType(String guid, TrassirServerInfo trassirServer) {
        String type;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(),
                    PathForRequest.STRING_USER_LIST + guid + "/",
                    trassirServer.getSessionId());
            UserType userType = restTemplate.getForObject(url, UserType.class);
            if (userType != null && userType.getError_code() == null && userType.getValues() != null) {
                type = userType.getType();
            } else {
                type = null;
            }
        } else {
            type = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return type;  //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
    }

    /* получаю группу пользователя */
    private String getUserGroup(String guid, TrassirServerInfo trassirServer) {
        String groupName;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(),
                    PathForRequest.STRING_USER_LIST + guid + "/" + "group",
                    trassirServer.getSessionId());
            UserGroup userGroup = restTemplate.getForObject(url, UserGroup.class);
            if (userGroup != null && userGroup.getError_code() == null && userGroup.getValue() != null
                    && userGroup.getValue().trim().length() > 0) {
                groupName = userGroup.getValue();
            } else {
                groupName = null;
            }
        } else {
            groupName = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return groupName;  //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
    }


    /* поиск юзеров по каналу */
    @GetMapping("/find/{id}")
    public List<String> findUsersByChannel(@PathVariable("id") String channelGuid) {
        List<String> usersName = new ArrayList<>();
        String channelName = trassirChannelService.findByGuid(channelGuid).getName();
        String serverName = trassirServerService.findByGuid("gZZKuo60").getServerName();
        usersName.add("Список пользователей с доступом к камере " + channelName + ":");
        List<TrassirUserRightsInfo> users = userRightsService.findUsersByChannel(channelGuid);

        for (TrassirUserRightsInfo user : users
        ) {
            if (user.getUserType().equals("User")) {
                usersName.add("Пользователь: " + user.getUserName() + ", Сервер: " + serverName);
            }

        }
        return usersName;
    }
}
