package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.repository.TrassirChannelRepo;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

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

}
