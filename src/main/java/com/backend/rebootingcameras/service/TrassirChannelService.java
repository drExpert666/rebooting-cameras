package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.repository.TrassirChannelRepo;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
public class TrassirChannelService {

    private TrassirChannelRepo trassirChannelRepo;

    @Autowired
    public TrassirChannelService(TrassirChannelRepo trassirChannelRepo) {
        this.trassirChannelRepo = trassirChannelRepo;
    }

    @Transactional
    public List<TrassirChannelInfo> updateAll(List<TrassirChannelInfo> channels) {
       return trassirChannelRepo.saveAll(channels);
    }

    @Transactional
    public TrassirChannelInfo findByGuid(String guid) {
        return trassirChannelRepo.findByGuidChannel(guid);
    }

    @Transactional
    public TrassirChannelInfo updateByChannel(TrassirChannelInfo channel) {
        System.out.println(channel);
        return trassirChannelRepo.save(channel);
    }

    @Transactional
    public List<TrassirChannelInfo> findAll() {
        return trassirChannelRepo.findAll();
    }

    @Transactional
    public Page<TrassirChannelInfo> findByParams(String serverId, String channelId, String channelName,
                                                 Integer signal, Long switchId, String channelIp, Boolean lostChannel, PageRequest pageRequest) {
        log.info(channelName);
        return trassirChannelRepo.findByParams(serverId, channelId, channelName, signal, switchId, channelIp, lostChannel, pageRequest);
    }

}
