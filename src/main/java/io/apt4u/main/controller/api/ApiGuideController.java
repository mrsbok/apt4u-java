package io.apt4u.main.controller.api;

import com.amazonaws.services.s3.model.S3Object;
import io.apt4u.main.service.DBConnService;
import io.apt4u.main.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiGuideController {
    @Autowired
    private DBConnService dbConnService;

    @Autowired
    private S3Service s3Service;

    @RequestMapping(value="/s3upload", method = RequestMethod.POST)
    public HashMap s3upload(MultipartHttpServletRequest request) {
        log.info("####s3upload#####");
        HashMap rtnVal = new HashMap();
        String error = null;

        Iterator itr = request.getFileNames();
        if (itr.hasNext()) {
            List mpf = request.getFiles((String) itr.next());
            for (int i = 0; i < mpf.size(); i++) {
                MultipartFile mf = ((MultipartFile) mpf.get(i));
                String filename = null;
                S3Service.FileGroupType groupType = S3Service.FileGroupType.PT_Trainer;
                try {
                    filename = s3Service.uploadWithUUID(mf, groupType);
                } catch (IOException e) {
                    e.printStackTrace();
                    error = "파일 업로드 실패!";
                }
                log.info("file upload to s3 : " + groupType.getValue() + " : " + filename);

                S3Object imgFileInfo = s3Service.getFileInfo(groupType.getValue() + filename);
                log.info("uploaded image file : " + imgFileInfo.toString());
                S3Object imgThumbFileInfo = s3Service.getFileInfo(groupType.getValue() + S3Service.thumbPath + filename);
                log.info("uploaded image thumb file : " + imgThumbFileInfo.toString());

                log.info("url : " + imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                BufferedImage imgBuf = null;
                String base64 = null;
                try {
                    imgBuf = ImageIO.read(imgFileInfo.getObjectContent());
                    base64 = S3Service.encodeBase64(imgBuf);
                    rtnVal.put("img_data", base64);
//                    log.info("base64 : " + base64);
                } catch (IOException e) {
                    e.printStackTrace();
                    error = "이미지파일 base64 변환 실패!";
                }

                HashMap infos = new HashMap();
                infos.put("filepath", groupType.getValue());
                infos.put("filename", filename);
                infos.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
                infos.put("filedata", base64);
                String ext = filename.substring(filename.lastIndexOf('.') + 1);
                infos.put("fileext", ext);
                rtnVal.put("infos", infos);
            }
        }

        if (error!=null) {
            rtnVal.put("result", false);
        }
        else {
            rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }


}
