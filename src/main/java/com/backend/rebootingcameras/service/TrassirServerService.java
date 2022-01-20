package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.repository.TrassirServerRepo;
import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class TrassirServerService {

    private TrassirServerRepo trassirServerRepo;

    @Autowired
    public TrassirServerService(TrassirServerRepo trassirServerRepo) {
        this.trassirServerRepo = trassirServerRepo;
    }

    @Transactional
    public List<TrassirServerInfo> updateAll(List<TrassirServerInfo> servers) {
        return trassirServerRepo.saveAll(servers);
    }



}
