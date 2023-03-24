package io.apt4u.main.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

@Service
@NoArgsConstructor
public class S3Service {
    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public static final String thumbPath = "thumb/";

    public enum FileGroupType {
        Store("Store/"),
        PT_Trainer("PT_Trainer/"),
        Product("Product/"),
        Board("Board/"),
        User("User/");

        private final String value;

        FileGroupType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public S3Object getFileInfo(String fullName){
        try {
            return s3Client.getObject(new GetObjectRequest(bucket, fullName));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            return null;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            return null;
        }
    }

    public String upload(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        try {

            s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            return null;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            return null;
        }
        return s3Client.getUrl(bucket, fileName).toString();
    }

    public String uploadWithUUID(MultipartFile file, FileGroupType fileGroupType) throws IOException {
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        UUID uuid = UUID.randomUUID();
        String newFileName = uuid + "." + ext;

        String path = "/";
        path = fileGroupType.getValue();

        try {

            s3Client.putObject(new PutObjectRequest(bucket, path + newFileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            InputStream is = makeThumbnail_s3(file, ext);
            s3Client.putObject(new PutObjectRequest(bucket, path + thumbPath + newFileName, is, null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            return null;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newFileName;
    }

    private InputStream makeThumbnail_s3(MultipartFile file, String fileExt) throws Exception {
        // 저장된 원본파일로부터 BufferedImage 객체를 생성합니다.
        BufferedImage srcImg = ImageIO.read(file.getInputStream());
        // 썸네일의 너비와 높이 입니다.
        int dw = 80, dh = 60;
        // 원본 이미지의 너비와 높이 입니다.
        int ow = srcImg.getWidth();
        int oh = srcImg.getHeight();
        // 원본 너비를 기준으로 하여 썸네일의 비율로 높이를 계산합니다.
        int nw = ow; int nh = (ow * dh) / dw;
        // 계산된 높이가 원본보다 높다면 crop이 안되므로
        // 원본 높이를 기준으로 썸네일의 비율로 너비를 계산합니다.
        if(nh > oh) { nw = (oh * dw) / dh; nh = oh; }
        // 계산된 크기로 원본이미지를 가운데에서 crop 합니다.
        BufferedImage cropImg = Scalr.crop(srcImg, (ow-nw)/2, (oh-nh)/2, nw, nh);

        // crop된 이미지로 썸네일을 생성합니다.
        BufferedImage destImg = Scalr.resize(cropImg, dw, dh);

        // 썸네일을 저장합니다. 이미지 이름 앞에 "THUMB_" 를 붙여 표시했습니다.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(destImg, fileExt, baos);
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static String encodeBase64(BufferedImage imgBuf) {
        String base64 = null;

        try {
            if (imgBuf == null) {
                base64 = null;
            } else {
                Base64 encoder = new Base64();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                ImageIO.write(imgBuf, "PNG", out);

                byte[] bytes = out.toByteArray();
                base64 = "data:image/png;base64," + new String(encoder.encode(bytes), "UTF-8");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        return base64;
    }
}
