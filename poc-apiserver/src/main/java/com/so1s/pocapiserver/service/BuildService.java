package com.so1s.pocapiserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class BuildService {

    public boolean saveFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String path = System.getProperty("user.dir");
            file.transferTo(new File(path+"/models/" + filename));
            System.out.println("\nsave : "+path+"/models/"+filename);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
