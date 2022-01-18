package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.utils.TrassirServerInfo;
import com.backend.rebootingcameras.utils.TrassirServersGuid;
import com.backend.rebootingcameras.utils.TrassirSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TrassirController {

    private final static String TRASSIR_MAIN_NAME = "trassir";
    private final static String SESSION_URL_WITH_SDK = "https://" + TRASSIR_MAIN_NAME + ":8080/login?password=12345";
    private final static String SESSION_URL_WITH_USER = "https://" + TRASSIR_MAIN_NAME + ":8080/login?username=Admin&password=Tiera6778351";
    private final static String SERVERS_GUID_METHOD = "settings/network/";

    private TrassirSession trassirSessionWithSdk; // данные сессии через id сессии
    private TrassirSession trassirSessionWithUser; // данные сессии через имя и пароль

    private TrassirServersGuid trassirServersGuid;
    private List<TrassirServerInfo> serverInfos = new ArrayList<>();

    private RestTemplate restTemplate; // DI для работы с запросами

    private ArrayList<String> serversIps; // список серверов

    @Autowired
    public TrassirController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    private ResponseEntity<TrassirSession> openSession(String sessionURL) {
        TrassirSession trassirSessionWithSdk = restTemplate.getForObject(sessionURL, TrassirSession.class);
        this.trassirSessionWithSdk = trassirSessionWithSdk;
        return ResponseEntity.ok(trassirSessionWithSdk);
    }

    // планировщик, запускающий сбор статистики с серверов Trassir
    @Scheduled(initialDelay = 3000, fixedDelayString = "PT01H")
    public void startCollectTrassirStats() {
        if (trassirSessionWithSdk == null) {
            openSession(SESSION_URL_WITH_SDK);
            System.out.println(this.trassirSessionWithSdk);
        }
        if (trassirSessionWithSdk.getError_code() != null) {
            openSession(SESSION_URL_WITH_SDK);
            System.out.println("Повторная попытка открыть сессию - ошибка: " + trassirSessionWithSdk.getError_code());
        } else {
            System.out.println("Сессия " + this.trassirSessionWithSdk.getSid() + " уже созана");
            trassirServersGuid = getTrassirServersGuid();
            System.out.println(trassirServersGuid);
        }
        if (trassirServersGuid != null) {
            getServersIp(trassirServersGuid);
            System.out.println(serverInfos);
        }

        System.out.println("Завершено");
    }


    // конструктор для создания get запроса из переданных параметров
    private String createUrl(String SID, String serverIP, String method) {
        return "https://" + serverIP + ":8080/" + method + "?sid=" + SID;
    }

    // получение guid серверов
    private TrassirServersGuid getTrassirServersGuid() {
        String serversGuid = createUrl(trassirSessionWithSdk.getSid(), TRASSIR_MAIN_NAME, SERVERS_GUID_METHOD);
        return restTemplate.getForObject(serversGuid, TrassirServersGuid.class);
    }

    // получение ip серверов
    private List<TrassirServerInfo> getServersIp(TrassirServersGuid trassirServersGuid) {
        if (trassirServersGuid == null) {
            return null;
        }
        for (String guid : trassirServersGuid.getSubdirs()) {
            if (!guid.equals("network_node_add")) {
                String serverIp = createUrl(trassirSessionWithSdk.getSid(), TRASSIR_MAIN_NAME, SERVERS_GUID_METHOD + guid + "/ip_address");
                System.out.println(Thread.currentThread().getName());
                serverInfos.add(restTemplate.getForObject(serverIp, TrassirServerInfo.class));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return serverInfos;
    }

    // получение списка каналов сервера
    private List<TrassirSession> getChannelsFromServer (TrassirServerInfo trassirServerInfo) {
        openSession(SESSION_URL_WITH_USER);
    }


}
