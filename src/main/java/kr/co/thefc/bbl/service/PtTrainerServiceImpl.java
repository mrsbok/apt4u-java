package kr.co.thefc.bbl.service;


import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

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
    HashMap data = new HashMap();
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
    HashMap data = new HashMap();
    HashMap data2 = new HashMap();
    try {
      data2.put("notice","프로필");
      data2.put("trainerIdx", idx);
      HashMap flag =  dbConnService.selectOne("approveFind", data2);
      System.out.println(flag);
      System.out.println("flag");
      System.out.println("flag");
      if(Objects.equals(flag.get("flag").toString(), "1")){
        rtnVal.put("message", "센터 승인후 조회 가능합니다");
      }else {
        data.put("trainerDetailInformation", dbConnService.selectIdx("selectDetailInformation", idx));
        data.put("imageList", dbConnService.selectIdxList("selectTrainerImages", idx));
      }


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

  //센터 인증 요청
  @Override
  public HashMap centerApprovedSave(Integer idx, CenterApproveForm centerApproveForm) {
    String error = null;
    centerApproveForm.setTrainerIdx(idx);
    try {
      HashMap form = new HashMap();

      String convertJson = gson.toJson(centerApproveForm);
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

  public List<HashMap> test(List<MultipartFile> request, String imageType) {
    String filename = null;
    String error = null;
    S3Service.FileGroupType groupType = S3Service.FileGroupType.Board;
    List fileList = new ArrayList<>();
    for (MultipartFile multipartFile : request) {
      if (!multipartFile.isEmpty()) {
        try {
          filename = s3Service.uploadWithUUID(multipartFile, groupType);

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
        } catch (IOException e) {
          e.printStackTrace();
          error = "파일 업로드 실패";
        }
      }
//    if (error != null) {
//      rtnVal.put("result", false);
//    } else {
//      rtnVal.put("result", true);
//    }
//    rtnVal.put("errorMsg", error);
//    System.out.println(fileList);
    }
    return fileList;
  }



  //프로필 저장
  @Override
  public HashMap profileSave(Integer ptTrainerIdx, List<MultipartFile> request) {
    String imageType = "프로필";
    List<HashMap> profileData = test(request, imageType);
    profileData.forEach(data  -> data.put("trainerIdx" , ptTrainerIdx) );
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


  //근무경력저장
  @Override
  public HashMap workExperienceSave(PtTrainerWorkExperienceForm ptTrainerWorkExperienceForm, List<MultipartFile> request) {
    String imageType = "근무 경력";
    List<HashMap> profileData = test(request, imageType);
    System.out.println(ptTrainerWorkExperienceForm.getTarinerIdx());
    String error = null;
    HashMap approveData = new HashMap();
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
      approveData.put("trainerIdx",ptTrainerWorkExperienceForm.getTarinerIdx());
      approveData.put("targerIdx" , idx);
      approveData.put("approvalStatus", "승인대기");
      approveData.put("notice",imageType);
      dbConnService.insert("centerApprovedSave", approveData);
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


  //근무경력 저장장
 @Override
  public HashMap workExperienceSelect(Integer idx) {
    String error = null;
    HashMap data = new HashMap();
    HashMap data2 = new HashMap();
    try {
//      data2.put("notice","프로필");
//      data2.put("trainerIdx", idx);
//      HashMap flag =  dbConnService.selectOne("approveFind", data2);

        data.put("imageList", dbConnService.selectIdxList("workExperienceFind", idx));


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

  //수상경력 저장
  @Override
  public HashMap awardWinningSave(PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList, List<MultipartFile> request) {
    String imageType = "수상 경력";
    List<HashMap> profileData = test(request, imageType);
    HashMap approveData = new HashMap();
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
      approveData.put("trainerIdx",pTtrainersAwardWinningFormsList.getTarinerIdx());
      approveData.put("targerIdx" , idx);
      approveData.put("approvalStatus", "승인대기");
      approveData.put("notice",imageType);
      dbConnService.insert("centerApprovedSave", approveData);
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
  @Override
  public HashMap awardWinningSelect(Integer idx) {
    String error = null;
    HashMap form = new HashMap();
    List<HashMap> ListData =  new ArrayList<>();
    try {
      form.put("trainerIdx",idx);
//      data2.put("notice","프로필");
//      data2.put("trainerIdx", idx);
//      HashMap flag =  dbConnService.selectOne("approveFind", data2);
      String convertJson = gson.toJson(form);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      ListData = dbConnService.select("AwardWinningFind", data);


    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("infos", ListData);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }


  //자격증 저장
  @Override
  public HashMap qualitificationSave(PTtrainersQualitificationForm pTtrainersQualitificationForm, List<MultipartFile> request) {
    String imageType = "자격증";
    List<HashMap> profileData = test(request, imageType);
    String error = null;
    HashMap approveData = new HashMap();
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

      approveData.put("trainerIdx",pTtrainersQualitificationForm.getTarinerIdx());
      approveData.put("targerIdx" , idx);
      approveData.put("approvalStatus", "승인대기");
      approveData.put("notice",imageType);
      dbConnService.insert("centerApprovedSave", approveData);

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

  @Override
  public HashMap qualitificationSelect(Integer idx) {
    String error = null;
    HashMap form = new HashMap();
    List<HashMap> ListData =  new ArrayList<>();
    try {
      form.put("trainerIdx",idx);
//      data2.put("notice","프로필");
//      data2.put("trainerIdx", idx);
//      HashMap flag =  dbConnService.selectOne("approveFind", data2);
      String convertJson = gson.toJson(form);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      ListData = dbConnService.select("qualitificationFind", data);


    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("infos", ListData);
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

  @Override
  public HashMap ptTrainersScheduleSave(PTScheduleForm ptScheduleForm) {
    String error = null;


    try {
      HashMap messagesData = new HashMap<>();
      String convertJson = gson.toJson(ptScheduleForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      Integer idx = dbConnService.insertWithReturnIntList("trainerScheduleSave",data) ;
      System.out.println(idx);
      System.out.println(ptScheduleForm);
      messagesData.put("typeIdx", idx);
      messagesData.put("messageType", 3);
      messagesData.put("content", "내용 어쩌고 저쩌고");
      messagesData.put("title", "<신규>운동 일정 등록");
      messagesData.put("receiverType", 1);
      messagesData.put("receiverIdx", ptScheduleForm.getUserIdx());
      messagesData.put("senderType", 3);
      messagesData.put("senderIdx", ptScheduleForm.getTarinerIdx());
      dbConnService.insert("trainerMessageSave",messagesData) ;


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

  @Override
  public HashMap ptTrainersScheduleSelect(PTScheduleForm ptScheduleForm) {
    String error = null;


    try {
      String convertJson = gson.toJson(ptScheduleForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      Integer idx = dbConnService.insertWithReturnIntList("trainerScheduleSelect",data) ;


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

  @Override
  public HashMap lessionSave(PTLessionForm ptLessionForm) {
    String error = null;

    try {
      String convertJson = gson.toJson(ptLessionForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("trainerLessionSave",data) ;


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


  @Override
  public HashMap userPtRecordSave(UserPtRecordForm UserPtRecordForm) {
    String error = null;

    try {

      String convertJson = gson.toJson(UserPtRecordForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      HashMap timeData  = dbConnService.selectOne("selectTime",data) ;
      String startTime = timeData.get("lesson_start_time").toString() + ":" + timeData.get("lesson_start_minute").toString();
      String endTime = timeData.get("lesson_end_time").toString() + ":" + timeData.get("lesson_end_minute").toString();

      data.put("startTime", startTime);
      data.put("endTime", endTime);
      Integer idx = dbConnService.insertWithReturnIntList("userPtRecordSave",data) ;

      data.put("recordIdx",idx);
      dbConnService.insert("userPtContentSave",data) ;

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



  @Override
  public HashMap getSchedule(UserPtRecordForm UserPtRecordForm) {
    String error = null;
    HashMap scheduleData = new HashMap<>();

    try {
      String convertJson = gson.toJson(UserPtRecordForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      scheduleData  = dbConnService.selectOne("selectTime",data) ;

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);

    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", scheduleData);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }


  @Override
  public HashMap updateSchedule(PTScheduleForm ptScheduleForm) {
    String error = null;


    try {
      String convertJson = gson.toJson(ptScheduleForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.update("updateSchedule",data) ;

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

  @Override
  public HashMap login(String userName, String password) {
    String error = null;
    HashMap loginData = new HashMap<>();
    try {
      loginData.put("userName",userName);
      loginData.put("password",password);
      String convertJson = gson.toJson(loginData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      loginData = dbConnService.selectOne("findUser",data) ;
      if(loginData != null) {
        if (Objects.equals(loginData.get("password").toString(), new PasswordCryptConverter().convertToDatabaseColumn(password))) {
          String token = new JwtProvider().jwtCreater(
              Integer.parseInt(loginData.get("idx").toString()),
              0,
              0
          );
          rtnVal.put("token", token);
          } else {
            error = "로그인 실패 패스워드 또는 ID를 확인하여 주십시오";
          }
        }else{
        error = "로그인 실패 패스워드 또는 ID를 확인하여 주십시오";
      }
      } catch (Exception e) {
        e.printStackTrace();
        error = "로그인 실패 패스워드 또는 ID를 확인하여 주십시오.";
      }


    if (error != null) {
      rtnVal.put("result", false);
      rtnVal.put("token", null);
    } else {
      rtnVal.put("result", true);
//      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap transactionSelect(TransactionForm transactionForm) {
    String error = null;
    List<HashMap> findData = new ArrayList<>();
    try {
      String convertJson = gson.toJson(transactionForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      findData = dbConnService.select("transactionSelect",data) ;
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터를 조회하지 못했읍니다";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", findData);
//      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap emailCheck(String userName) {
    String error = null;
    String message = null;
    HashMap emailData = new HashMap<>();
    try {
      emailData.put("username" , userName);
      String convertJson = gson.toJson(emailData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      System.out.println(data);
      HashMap flag = dbConnService.selectOne("findEmail",data) ;
      if(flag == null){
        message = "사용 가능한 아이디입니다";
        rtnVal.put("flag", true);
        rtnVal.put("message", message);
      }else {
        message = "중복된 아이디 입니다.";
        rtnVal.put("flag", false);
        rtnVal.put("message", message);
      }
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터를 조회하지 못했읍니다";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
//      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap deleteAccount(DeleteTrainerForm deleteTrainerForm) {
    String error = null;
    String message = null;
    HashMap emailData = new HashMap<>();
    HashMap result = new HashMap();
    try {
      String convertJson = gson.toJson(deleteTrainerForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      System.out.println(data);
      result = dbConnService.selectOne("findTrainer",data) ;
      System.out.println(data);
      dbConnService.insert("deleteAccountSave", data);
      dbConnService.delete("deleteTrainer", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터를 조회하지 못했읍니다";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", result);
//      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap completePayment(Integer transactionIdx, Integer trainerIdx) {
    String error = null;
    String message = null;
    HashMap requestData = new HashMap<>();
    HashMap result = new HashMap();
    requestData.put("transactionIdx" , transactionIdx);
    requestData.put("trainerIdx", trainerIdx);
    try {
      String convertJson = gson.toJson(requestData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      System.out.println(data);
      dbConnService.update("completePayment",data) ;
      System.out.println(data);
      result = dbConnService.selectOne("findTransaction", data);
      System.out.println(result);
      System.out.println("=========");
      System.out.println(data);
      dbConnService.insert("ticketValidSave", result);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터를 조회하지 못했읍니다";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", result);
//      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap userPtRecordSelect(UserPtRecordForm UserPtRecordForm) {
    String error = null;
    HashMap data2 = new HashMap<>();
    try {
      String convertJson = gson.toJson(UserPtRecordForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
       data2 = dbConnService.selectOne("userPtRecordSelect",data) ;

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error != null) {
      rtnVal.put("result", false);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", data2);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap updateRecords(UserPtRecordForm UserPtRecordForm) {
    String error = null;


    try {
      String convertJson = gson.toJson(UserPtRecordForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.update("updateRecords",data) ;

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
