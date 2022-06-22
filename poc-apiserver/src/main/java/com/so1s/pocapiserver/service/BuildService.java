package com.so1s.pocapiserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class BuildService {

    public boolean saveFile(String itemName, MultipartFile file) throws IOException {
        try {
            String filename = file.getOriginalFilename();
            String path = System.getProperty("user.dir");
            file.transferTo(new File(path + "/" + itemName));
            System.out.println("파일 업로드 성공 to ["+path + "/" + itemName+"]");
        } catch (Exception e) {
            System.out.println("파일 업로드 장애 발생 " + e);
            return false;
        }

        return true;
    }

    public boolean buildImage() {


        return true;
    }
}
