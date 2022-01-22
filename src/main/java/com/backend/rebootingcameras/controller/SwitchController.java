package com.backend.rebootingcameras.controller;

import com.backend.rebootingcameras.entity.Switch;
import com.backend.rebootingcameras.search.SwitchSearchValues;
import com.backend.rebootingcameras.service.SwitchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/switch")
public class SwitchController {

    private SwitchService switchService;

    @Autowired
    public SwitchController(SwitchService switchService) {
        this.switchService = switchService;
    }

    /** CRUD операции */

    @GetMapping("/all")
    public ResponseEntity<List<Switch>> findAll() { //todo добавить проверки
        return new ResponseEntity<>(switchService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Switch> findById(@PathVariable Long id) { //todo добавить проверки
        return new ResponseEntity<>(switchService.findById(id), HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Switch> deleteById(@PathVariable Long id) { //todo добавить проверки
        switchService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Switch> update(@RequestBody Switch s) { //todo добавить проверки
        return new ResponseEntity<>(switchService.update(s), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Switch> add(@RequestBody Switch s) { //todo добавить проверки
        return new ResponseEntity<>(switchService.update(s), HttpStatus.OK);
    }

    /** другие операции */

    /* поиск по всем параметрам */
    @PostMapping("/search")
    public ResponseEntity<List<Switch>> search(@RequestBody SwitchSearchValues searchValues) { //todo добавить проверки
        return new ResponseEntity<>(switchService.findByParams(searchValues.getName(), searchValues.getIp(), searchValues.getDescription()), HttpStatus.OK);
    }



}
