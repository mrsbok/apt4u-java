package kr.co.thefc.bbl.service;


import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PtTrainerServiceImpl implements PtTrainerService {

  private Gson gson = new Gson();
  HashMap rtnVal = new HashMap();

  @Autowired
  private DBConnService dbConnService;


  @Autowired
  private S3Service s3Service;

  //트레이너 등록
  @Override
  public HashMap trainerSave(PtTrainerForm ptTrainerForm) {
    String error = null;
    try {
      ptTrainerForm.setPassword(
          new PasswordCryptConverter()
              .convertToDatabaseColumn(ptTrainerForm.getPassword()
              )
      );
      String convertJson = gson.toJson(ptTrainerForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("trainerSave", data);
      dbConnService.insert("trainerInfoSave", data);
      dbConnService.insert("trainerAuthenticate", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //트레이너 상세정보 등록
  @Override
  public HashMap trainerInfoDetailSave(PtTrainerProfileForm ptTrainerProfileForm) {
    String error = null;
    try {
      String convertJson = gson.toJson(ptTrainerProfileForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("trainerInfoDetailSave", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //1회 체험가 등록
  @Override
  public HashMap oneDayAmountSave(OneDayAmountForm oneDayAmountForm) {
    String error = null;

    try {
      String convertJson = gson.toJson(oneDayAmountForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("oneDayAmountSave", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //트레이너 정보조회
  @Override
  public HashMap selectInformation(Integer idx) {
    String error = null;
    HashMap data  = new HashMap();
    try {

      data = dbConnService.selectIdx("selectInformation", idx);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("infos", data);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //트레이너 정보 상세 조회
  @Override
  public HashMap selectDetailInformation(Integer idx) {
    String error = null;
    HashMap data  = new HashMap();
    try {

      data.put("trainerDetailInformation",dbConnService.selectIdx("selectDetailInformation", idx));
      data.put("imageList",dbConnService.selectIdxList("selectTrainerImages", idx));

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("infos", data);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //트레이너 정보수정
  @Override
  public HashMap updateInformation(PtTrainerForm ptTrainerForm) {
    String error = null;
    try {
      String convertJson = gson.toJson(ptTrainerForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.update("updateInformation", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //트레이너 패스워드 업데이트
  @Override
  public HashMap updatePassword(Integer idx, String password) {
    String error = null;
    try {

      HashMap form = new HashMap();
      form.put("idx", idx);
      form.put("password",
          new PasswordCryptConverter()
              .convertToDatabaseColumn(
                  password
              )
      );
      String convertJson = gson.toJson(form);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.update("updatePassword", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //소속 센터 변경
  @Override
  public HashMap centerApprovedSave(Integer idx, Integer AffiliateCenter, String ApprovalStatus) {
    String error = null;
    try {
      HashMap form = new HashMap();
      form.put("idx", idx);
      form.put("affiliateCenter", AffiliateCenter);
      form.put("approvalStatus", ApprovalStatus);
      String convertJson = gson.toJson(form);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("centerApprovedSave", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //구매 정보 저장
  @Override
  public HashMap buyInformtaionSave(List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm) {
    String error = null;
    try {
      dbConnService.insertList("buyInformationSave", ptTrainerBuyInformationForm);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //요금 정보 저장
  @Override
  public HashMap feeInformationSave(PtFeeInformationDetailForm ptFeeInformationDetailForm) {
    String error = null;
    System.out.println("1" + ptFeeInformationDetailForm.ptFeeInformtaionForms);
    try {
      String convertJson = gson.toJson(ptFeeInformationDetailForm.ptFeeInformtaionForms);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      List listData = new ArrayList<>();
      List listData2 = new ArrayList<>();
      Integer idx = dbConnService.insertWithReturnIntList("feeInformationSave", data);

      ptFeeInformationDetailForm.ptUsePriceFormList.forEach(
          ptUsePriceForm
              -> {
            ptUsePriceForm.setIdx(
                idx
            );
            listData2.add(ptUsePriceForm);
          }
      );
      System.out.println("두번째" + listData2);
      dbConnService.insertList("usePriceSave", listData2);
      ptFeeInformationDetailForm.ptUsePriceFormList.forEach(
          usePriceData
              -> usePriceData.getPtUsePriceDetailForm().forEach(
              ptUsePriceDetailForm
                  -> {
                ptUsePriceDetailForm.setIdx(usePriceData.getIdx());
                listData.add(ptUsePriceDetailForm);
              }

          )
      );

      dbConnService.insertList("usePriceDetailSave", listData);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //요금 정보 조회
  @Override
  public HashMap feeInformationSelect(Integer ptTrainerIdx) {
    String error = null;
    List<HashMap> list = new ArrayList<>();
    try {

      list = dbConnService.selectIdxList("oneDayAmountSelect", ptTrainerIdx);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("infos", list);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

//프로필 저장
  public List<HashMap> s3upload(MultipartHttpServletRequest request, String imageType) {
    log.info("####s3upload#####");
    HashMap rtnVal = new HashMap();
    String error = null;
    List fileList = new ArrayList<>();
    List filePath = new ArrayList<>();
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
        infos.put("imagetype", imageType);
        infos.put("fileurl", imgFileInfo.getObjectContent().getHttpRequest().getURI().toString());
//        infos.put("filedata", base64);
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        infos.put("fileext", ext);
        fileList.add(infos);
        rtnVal.put("infos", infos);
      }
    }

    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    System.out.println(fileList);
    return fileList;
  }

  //프로필 저장
  @Override
  public HashMap profileSave(Integer ptTrainerIdx, MultipartHttpServletRequest request) {
    String imageType = "프로필";
    List<HashMap> profileData = s3upload(request, imageType);
    String error = null;
    try {
      dbConnService.insertList("trainerProfileSave", profileData);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //   dbConnService.insertList("trainerAwardWinningSave", ptTrainerDetailForm.pTtrainersAwardWinningFormsList);
//      dbConnService.insertList("trainerQualitificationSave", ptTrainerDetailForm.pTtrainersQualitificationFormList);

  //근무경력저장
  @Override
  public HashMap workExperienceSave(PtTrainerWorkExperienceForm ptTrainerWorkExperienceForm, MultipartHttpServletRequest request) {
    String imageType = "근무 경력";
    List<HashMap> profileData = s3upload(request, imageType);
    String error = null;
    try {

      String convertJson = gson.toJson(ptTrainerWorkExperienceForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      Integer idx = dbConnService.insertWithReturnIntList("trainerWorkExperienceSave", data);
      System.out.println("---------------");
      System.out.println(profileData);
      profileData.forEach(
          datax -> datax.put("workExprienceIseq" , idx)

      );
      System.out.println("---------------");
      System.out.println(profileData);
      dbConnService.insertList("trainerProfileSave", profileData);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //수상경력 저장
  @Override
  public HashMap awardWinningSave(PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList, MultipartHttpServletRequest request) {
    String imageType = "수상 경력";
    List<HashMap> profileData = s3upload(request, imageType);
    String error = null;
    try {
      String convertJson = gson.toJson(pTtrainersAwardWinningFormsList);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      Integer idx = dbConnService.insertWithReturnIntList("trainerAwardWinningSave", data);
      System.out.println("---------------");
      System.out.println(profileData);
      profileData.forEach(
          datax -> datax.put("awardWinningIseq" , idx)

      );
      System.out.println("---------------");
      System.out.println(profileData);
      dbConnService.insertList("trainerProfileSave", profileData);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //자격증 저장
  @Override
  public HashMap qualitificationSave(PTtrainersQualitificationForm pTtrainersQualitificationForm, MultipartHttpServletRequest request) {
    String imageType = "자격증";
    List<HashMap> profileData = s3upload(request, imageType);
    String error = null;
    try {

      String convertJson = gson.toJson(pTtrainersQualitificationForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      Integer idx = dbConnService.insertWithReturnIntList("trainerQualitificationSave",data) ;
      System.out.println("---------------");
      System.out.println(profileData);
      profileData.forEach(
          datax -> datax.put("qualitificationIseq" , idx)

      );
      System.out.println("---------------");
      System.out.println(profileData);
      dbConnService.insertList("trainerProfileSave", profileData);



    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  //자격사항 삭제
  @Override
  public HashMap qualitificationDelete(DeleteQualitificationForm deleteQualitificationForm) {
    String error = null;


    try {
      String convertJson = gson.toJson(deleteQualitificationForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
	    switch (deleteQualitificationForm.getImageType()) {
        case "프로필" :

        case "수상 경력" :
          dbConnService.delete("deleteImage", data);
          dbConnService.delete("deleteAward", data);
        case "근무 경력" :
          dbConnService.delete("deleteImage", data);
          dbConnService.delete("deleteWork", data);
        case "자격증" :
          dbConnService.delete("deleteImage", data);
          dbConnService.delete("deleteQuailtification", data);
      }
//      dbConnService.insertList("trainerProfileSave", profileData);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

}