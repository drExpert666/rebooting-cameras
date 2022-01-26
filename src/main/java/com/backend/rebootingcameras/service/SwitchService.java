package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.repository.SwitchRepo;
import com.backend.rebootingcameras.search.SwitchSearchValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SwitchService {

    private SwitchRepo switchRepo;

    @Autowired
    public SwitchService(SwitchRepo switchRepo) {
        this.switchRepo = switchRepo;
    }

    /** CRUD операции */

    @Transactional
    public List<Switch> findAll() {
        return switchRepo.findAll();
    }

    @Transactional
    public Switch findById(Long id) {
       if (switchRepo.findById(id).isPresent()) {
           return switchRepo.findById(id).get();
       }
        return null;
    }

    @Transactional
    public void deleteById(Long id) {
       switchRepo.deleteById(id);
    }

    @Transactional
    public Switch update(Switch s) {
       return switchRepo.save(s);
    }

    /** поиск */

    @Transactional
    public List<Switch> findByParams(String name, String ip, String description) {
        return switchRepo.findByParams(name, ip, description);
    }

    @Transactional //todo добавить проверку
    public Switch findByIp(String ip) {
       return switchRepo.findSwitchByIp(ip);
    }



}
