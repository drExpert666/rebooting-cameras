package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.repository.UserRightsRepo;
import com.backend.rebootingcameras.trassir_models.TrassirUserRightsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRightsService {

    private UserRightsRepo userRightsRepo;
    @Autowired
    public UserRightsService(UserRightsRepo userRightsRepo) {
        this.userRightsRepo = userRightsRepo;
    }

    @Transactional
    public TrassirUserRightsInfo update(TrassirUserRightsInfo trassirUserRights) {
       return userRightsRepo.save(trassirUserRights);
    }

    @Transactional
    public List<TrassirUserRightsInfo> saveAll(List<TrassirUserRightsInfo> trassirUserRights) {
        return userRightsRepo.saveAll(trassirUserRights);
    }

    @Transactional
    public List<TrassirUserRightsInfo> findUsersByChannel(String channel) {
        return userRightsRepo.findUsersByChannel(channel);
    }


    @Transactional
    public List<TrassirUserRightsInfo> findByGroupGuid(String groupId) {
        return userRightsRepo.findAllByGroupId(groupId);
    }

    @Transactional
    public TrassirUserRightsInfo findById(String guid) {
        return userRightsRepo.findByGuid(guid);
    }



}
