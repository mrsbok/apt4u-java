package kr.co.thefc.bbl.service;


import com.google.gson.Gson;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class PtTrainerServiceImpl implements PtTrainerService {

    private Gson gson =  new Gson();
    HashMap rtnVal = new HashMap();

    @Autowired
    private DBConnService dbConnService;

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
        HashMap data = gson.fromJson(convertJson,HashMap.class);
        dbConnService.insert("trainerSave", data);
        dbConnService.insert("trainerInfoSave", data);
        dbConnService.insert("trainerAuthenticate", data);
      } catch (Exception e) {
        e.printStackTrace();
        error = "데이터 저장 실패.";
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

    //트레이너 상세정보 등록
    @Override
    public HashMap trainerInfoDetailSave(PtTrainerDetailForm ptTrainerDetailForm) {
      String error = null;
      try {
        String convertJson = gson.toJson(ptTrainerDetailForm.ptTrainerProfileForm);
        HashMap data = gson.fromJson(convertJson,HashMap.class);
        dbConnService.insert("trainerInfoDetailSave", data);
        dbConnService.insertList("trainerWorkExperienceSave",ptTrainerDetailForm.ptTrainerWorkExperienceFormList);
        dbConnService.insertList("trainerAwardWinningSave", ptTrainerDetailForm.pTtrainersAwardWinningFormsList);
        dbConnService.insertList("trainerQualitificationSave", ptTrainerDetailForm.pTtrainersQualitificationFormList);
      } catch (Exception e) {
        e.printStackTrace();
        error = "데이터 저장 실패.";
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

  //1회 체험가 등록
  @Override
  public HashMap oneDayAmountSave(OneDayAmountForm oneDayAmountForm) {
    String error = null;

    try {
      String convertJson = gson.toJson(oneDayAmountForm);
      HashMap data = gson.fromJson(convertJson,HashMap.class);
      dbConnService.insert("oneDayAmountSave", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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

  //트레이너 정보조회
  @Override
  public HashMap selectInformation(Integer idx) {
    String error = null;
    try {

      rtnVal = dbConnService.selectIdx("selectInformation", idx);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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
  //트레이너 정보수정
  @Override
  public HashMap updateInformation(PtTrainerForm ptTrainerForm) {
    String error = null;
    try {
      String convertJson = gson.toJson(ptTrainerForm);
      HashMap data = gson.fromJson(convertJson,HashMap.class);
      dbConnService.update("updateInformation", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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

  //트레이너 패스워드 업데이트
  @Override
  public HashMap updatePassword(Integer idx,String password) {
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
      HashMap data = gson.fromJson(convertJson,HashMap.class);
      dbConnService.update("updatePassword", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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

  //소속 센터 변경
  @Override
  public HashMap centerApprovedSave(Integer idx,Integer AffiliateCenter,String ApprovalStatus) {
    String error = null;
    try {
      HashMap form = new HashMap();
      form.put("idx", idx);
      form.put("affiliateCenter", AffiliateCenter);
      form.put("approvalStatus", ApprovalStatus);
      String convertJson = gson.toJson(form);
      HashMap data = gson.fromJson(convertJson,HashMap.class);
      dbConnService.insert("centerApprovedSave", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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

  @Override
  public HashMap buyInformtaionSave(List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm) {
    String error = null;
    try {
      dbConnService.insertList("buyInformationSave", ptTrainerBuyInformationForm);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
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

  @Override
  public HashMap feeInformationSave(PtFeeInformationDetailForm ptFeeInformationDetailForm) {
    String error = null;
    System.out.println("1" +ptFeeInformationDetailForm.ptFeeInformtaionForms);
    try {
      String convertJson = gson.toJson(ptFeeInformationDetailForm.ptFeeInformtaionForms);
      HashMap data = gson.fromJson(convertJson,HashMap.class);
      List listData = new ArrayList<>();
      List listData2 = new ArrayList<>();
      Integer idx  = dbConnService.insertWithReturnIntList("feeInformationSave", data);
      System.out.println("1" +ptFeeInformationDetailForm.ptFeeInformtaionForms);
      System.out.println("2" + ptFeeInformationDetailForm.ptFeeInformtaionForms);
      ptFeeInformationDetailForm.ptUsePriceFormList.forEach(
          ptUsePriceForm
              ->{
            ptUsePriceForm.setIdx(
                  idx
              );
            listData2.add(ptUsePriceForm);
          }
      );
      System.out.println("두번째" + listData2);
      dbConnService.insertList("usePriceSave",listData2);
      ptFeeInformationDetailForm.ptUsePriceFormList.forEach(
          usePriceData
              -> usePriceData.getPtUsePriceDetailForm().forEach(
              ptUsePriceDetailForm
                  ->{
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
    if (error!=null) {
      rtnVal.put("result", false);
    }
    else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public List<HashMap> feeInformationSelect(Integer ptTrainerIdx) {
    String error = null;
    List<HashMap> list = new ArrayList<>();
    try {

      list = dbConnService.selectIdxList("oneDayAmountSelect", ptTrainerIdx);

    } catch (Exception e) {
      e.printStackTrace();
      error = "데이터 저장 실패.";
    }
    if (error!=null) {
      rtnVal.put("result", false);
    }
    else {
      rtnVal.put("result", true);
    }
    rtnVal.put("errorMsg", error);
    return list;
    }

}