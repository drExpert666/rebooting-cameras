package com.backend.rebootingcameras.service;

import com.backend.rebootingcameras.entity.Channel;
//import com.backend.rebootingcameras.repository.ChannelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Service
//public class ChannelService{

//    private ChannelRepo channelRepo;
//
//    @Autowired
//    public ChannelService(ChannelRepo channelRepo) {
//        this.channelRepo = channelRepo;
//    }
//
//    @Transactional
//    public Channel updateChannel(Channel channel) {
//        return channelRepo.save(channel);
//    }
//
//    @Transactional
//    public List<Channel> findAll() {
//        return channelRepo.findAll();
//    }
//
//}
