package com.backend.rebootingcameras.repository;

import com.backend.rebootingcameras.entity.Switch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwitchRepo extends JpaRepository<Switch, Long> {


    @Query("select s from Switch s where (:name is null or :name = '' or lower(s.name) like lower(concat('%',:name,'%') )) " +
            "and (:ip is null or :ip = '' or lower(s.ip) like lower(concat('%',:ip,'%'))) " +
            "and (:description is null or :description = '' or lower(s.description) like lower(concat('%',:description,'%'))) order by s.name")
    List<Switch> findByParams(@Param("name")String name,
                              @Param("ip")String ip,
                              @Param("description") String description);

    Switch findSwitchByIp(String ip);


    List<Switch> findAllByOrderByIp();
}
