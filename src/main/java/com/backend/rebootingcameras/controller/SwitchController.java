package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.search.SwitchSearchValues;
import com.backend.rebootingcameras.service.SwitchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/switch")
@CrossOrigin() // разрешить для этого ресурса получать данные с бэкенда
public class SwitchController {

    private SwitchService switchService;

    @Autowired
    public SwitchController(SwitchService switchService) {
        this.switchService = switchService;
    }

    /** CRUD операции */

    @GetMapping("/all")
    public ResponseEntity<List<Switch>> findAll() {
        return new ResponseEntity<>(switchService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Switch> findById(@PathVariable Long id) {
        Switch switchServiceById = switchService.findById(id);
        if (switchServiceById != null) {
            return new ResponseEntity<>(switchService.findById(id), HttpStatus.OK);
        }
        else {
            return new ResponseEntity("id = " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Switch> deleteById(@PathVariable Long id) {
       try {
           switchService.deleteById(id);
       } catch (EmptyResultDataAccessException e) {
           return new ResponseEntity("id = " + id + " not found", HttpStatus.NOT_FOUND);
       }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Switch> update(@RequestBody Switch s) {
        if (s.getId() == null || s.getId() == 0) {
            return new ResponseEntity("id must be fill", HttpStatus.NOT_ACCEPTABLE);
        }
        else {
            return new ResponseEntity(switchService.update(s), HttpStatus.OK);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Switch> add(@RequestBody Switch s) {
        if (s.getId() != null && s.getId() != 0) {
            return new ResponseEntity("id must be empty", HttpStatus.NOT_ACCEPTABLE);
        }
        else {
            return new ResponseEntity(switchService.update(s), HttpStatus.OK);
        }
    }

    /** другие операции */

    /* поиск по всем параметрам */
    @PostMapping("/search")
    public ResponseEntity<List<Switch>> search(@RequestBody SwitchSearchValues searchValues) {
        return new ResponseEntity<>(switchService.findByParams(searchValues.getName(), searchValues.getIp(),
                searchValues.getDescription()), HttpStatus.OK);
    }



}
