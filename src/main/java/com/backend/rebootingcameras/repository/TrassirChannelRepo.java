package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.trassir_models.TrassirChannelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrassirChannelRepo extends JpaRepository<TrassirChannelInfo, String> {


}
