package com.so1s.pocapiserver.controller;

import com.so1s.pocapiserver.service.BuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class BuildController {

    private BuildService buildService;

    @Autowired
    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName, @RequestParam MultipartFile attachFile) throws IOException {
        return buildService.saveFile(itemName, attachFile)? "/uploadSuccess" : "/uploadFailed";
    }
}
