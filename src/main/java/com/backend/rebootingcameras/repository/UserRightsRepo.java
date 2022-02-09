package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.trassir_models.TrassirUserRightsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRightsRepo extends JpaRepository<TrassirUserRightsInfo, String> {

}
