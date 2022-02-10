package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.search.UserSearchValues;
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


    @Scheduled(initialDelay = 1000, fixedDelayString = "PT10S")
    public void startCollectUserRights() {
        System.out.println("----------------------------------------");
        System.out.println("Начало сбора информации о пользователях");
        /* записываю данные о сервере из БД в переменную */
        String guidServer = "gZZKuo60";//todo получить guid сервера из БД
        TrassirServerInfo trassirServer = trassirServerService.findByGuid(guidServer);

        /* получаю айди пользователей из трассира */
        List<String> usersGuid = fillUserRights(trassirServer);

        List<TrassirUserRightsInfo> userRights = new ArrayList<>();


//        /* тест */
//        String guid = "BUKrTw6c";
//        /*  */
//        Integer baseRights = getBaseUserRights(guid, trassirServer);
//        /* */
//        String aclRights = getAclUserRights(guid, trassirServer);
//        /* получаю имя пользователя */
//        String userName = getUserName(guid, trassirServer);
//        if (guid != null && baseRights != null && aclRights != null) {
//            System.out.println("----------------------------");
//            System.out.println("Добавляю guid: " + guid);
//            TrassirUserRightsInfo trassirUserRights =
//                    new TrassirUserRightsInfo(guid, userName, baseRights, aclRights, null, trassirServer.getGuid());
//            System.out.println("----------------------------");
//            System.out.println("Присвоены значения: " + trassirUserRights);
//            userRights.add(trassirUserRights);
//        } else {
//            System.out.println("Ошибка, гуид пользователя: " + guid + " " + userName);
//            System.out.println("Ошибка, baseRights пользователя: " + baseRights);
//            System.out.println("Ошибка, aclRights пользователя: " + aclRights);
//        }


        /* заполняю данные о каждом пользователе из трассира */
        if (usersGuid != null) {
            for (String userId : usersGuid) {
                /*  */
                Integer baseRights = getBaseUserRights(userId, trassirServer);
                /* */
                String aclRights = getAclUserRights(userId, trassirServer);
                /* получаю имя пользователя */
                String userName = getUserName(userId, trassirServer);
                if (userId != null && baseRights != null && aclRights != null) {
                    System.out.println("----------------------------");
                    System.out.println("Добавляю userId: " + userId);
                    TrassirUserRightsInfo trassirUserRights =
                            new TrassirUserRightsInfo(userId, userName, baseRights, aclRights, null, trassirServer.getGuid());
                    System.out.println("----------------------------");
                    System.out.println("Присвоены значения: " + trassirUserRights);
                    userRights.add(trassirUserRights);
                } else {
                    System.out.println("Ошибка, гуид пользователя: " + userId + " " + userName);
                    System.out.println("Ошибка, baseRights пользователя: " + baseRights);
                    System.out.println("Ошибка, aclRights пользователя: " + aclRights);
                }
            }
        }
//        System.out.println("-------------------------------------");
//        System.out.println("Присвоены значения: " + userRights);
        List<TrassirUserRightsInfo> trassirUserRightsInfos = userRightsService.saveAll(userRights);
        System.out.println("-------------------------------------");
        System.out.println("Сохраняю в БД и получаю обратно список: " + trassirUserRightsInfos);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        trassirUserRightsInfos.forEach(ur -> {
            String channels = parserUserRights.getChannelsFromUserTrassir(ur.getBaseRights(), ur.getAcl());
            System.out.println("-----------------------------------------");
            System.out.println("Распарсенная строка каналов: " + channels);
            ur.setChannels(channels);
            System.out.println("------------------------------------");
            System.out.println("Список каналов пользователя " + ur.getGuid() + " " + ur.getUserName() + ": " + ur.getChannels());
            System.out.println("Количество каналов пользователя " + ur.getGuid() + " " + ur.getUserName() + ": " +
                    ((ur.getChannels().length() == 0) ? 0 : (ur.getChannels().split(",").length)));
            /* записываю список каналов в БД */
            userRightsService.update(ur);
        });
    }


    /* получаю айди пользователей из трассира */
    private List<String> fillUserRights(TrassirServerInfo trassirServer) {
        //todo подумать что лучше делать, если время сессии истекло
        List<String> usersGuid;
        if (trassirController.checkLastSessionUpdate(trassirServer)) {
            String url = String.format(PathForRequest.STRING_FOR_FORMAT,
                    trassirServer.getServerIP(), PathForRequest.STRING_USER_LIST, trassirServer.getSessionId());
            UserList userList = restTemplate.getForObject(url, UserList.class);
            System.out.println(userList);
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
                System.out.println("Обновляю сессию для сервера " + trassirServer.getGuid());
                trassirServer.setSessionId(session.getSid());
                trassirServer.setLustUpdate(new Date());
                System.out.println(trassirServer);
                trassirServerService.updateByServer(trassirServer);
            }
            usersGuid = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return usersGuid; //todo тут возвращется null не из-за ошибки, а из-за просроченной сессии, нужно изменить
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
            System.out.println(userRightsBaseRights);
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
            System.out.println(userRightsAcl);
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
            System.out.println(userName);
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

    /* поиск юзеров по каналу */
    @GetMapping("/find/{id}")
    public List<String> findUsersByChannel(@PathVariable("id") String channelGuid) {
        List<String> usersName = new ArrayList<>();
        System.out.println(channelGuid);
        List<TrassirUserRightsInfo> users = userRightsService.findUsersByChannel(channelGuid);
        for (TrassirUserRightsInfo user: users
             ) {
            usersName.add(user.getUserName());
        }
        return usersName;
    }
}
