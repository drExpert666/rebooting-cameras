package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.data.PathForRequest;
import com.backend.rebootingcameras.service.SwitchService;
import com.backend.rebootingcameras.service.TrassirChannelService;
import com.backend.rebootingcameras.service.TrassirServerService;
import com.backend.rebootingcameras.trassir_requests.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/rights")
public class UserRightsController {


    /* DI */
    private RestTemplate restTemplate; // DI для работы с запросами
    private TrassirChannelService trassirChannelService;
    private TrassirServerService trassirServerService;
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


    @Scheduled(initialDelay = 1000, fixedDelayString = "PT10M")
    public void startCollectUserRights() {

    }


    private void fillUserRights() {
        //todo получить guid сервера из БД
        //todo проверить наличие открытой сесии в БД

        String guidServer = "gZZKuo60";//todo получить guid сервера из БД

        String url = String.format(PathForRequest.STRING_FOR_FORMAT, guidServer, PathForRequest.STRING_BASE_RIGHTS, "sessionID");

        restTemplate.getForObject("url", UserList.class);

    }


}
