package com.so1s.pocapiserver.controller;

import com.so1s.pocapiserver.service.BuildService;
import com.so1s.pocapiserver.service.KubernetesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ModelUpdateController {

    private BuildService buildService;
    private KubernetesService kubernetesService;

    @Autowired
    public ModelUpdateController(BuildService buildService, KubernetesService kubernetesService) {
        this.buildService = buildService;
        this.kubernetesService = kubernetesService;
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam MultipartFile attachFile) {
        System.out.print("SAVE Model...");
        if(!buildService.saveFile(attachFile)) {
            return "/uploadFailed";
        }
        System.out.println(" Finish!");

        System.out.print("Build Job...");
        if(!kubernetesService.buildImage()){
            return "/buildFailed";
        }
        System.out.println(" Finish!");

        System.out.print("Deploy New Inference...");
        if(!kubernetesService.updateInferenceServer()){
            return "/updateFailed";
        }
        System.out.println(" Finish!");

        return "/Success";
    }
}
