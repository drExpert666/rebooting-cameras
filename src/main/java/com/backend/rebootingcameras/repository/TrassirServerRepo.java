package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.trassir_models.TrassirServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrassirServerRepo extends JpaRepository<TrassirServerInfo, String> {

    TrassirServerInfo findByGuid(String guid);

}
