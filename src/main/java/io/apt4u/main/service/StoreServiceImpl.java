package io.apt4u.main.service;


import com.google.gson.Gson;
import io.apt4u.main.converter.JwtProvider;
import io.apt4u.main.converter.PasswordCryptConverter;
import io.apt4u.main.model.storeForm.StoreForm;
import io.apt4u.main.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class StoreServiceImpl implements StoreService {

  private Gson gson = new Gson();
  HashMap rtnVal = new HashMap();

  @Autowired
  private DBConnService dbConnService;


  @Autowired
  private S3Service s3Service;


  @Override
  public HashMap storeRegister(StoreForm storeForm) {
    String error = null;
    try {
      storeForm.setPassword(
          new PasswordCryptConverter()
              .convertToDatabaseColumn(storeForm.getPassword()
              )
      );
      String convertJson = gson.toJson(storeForm);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.insert("storeSave", data);
      dbConnService.insert("storeInfoSave", data);
      dbConnService.insert("storeAuthenticateSave", data);
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
  public HashMap emailCheck(String userName) {
    String error = null;
    String message = null;
    HashMap emailData = new HashMap<>();
    try {
      emailData.put("username" , userName);
      String convertJson = gson.toJson(emailData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      System.out.println(data);
      HashMap flag = dbConnService.selectOne("storeCheckEmail",data) ;
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
  public HashMap login(String userName, String password) {
    String error = null;
    HashMap loginData = new HashMap<>();
    try {
      loginData.put("userName",userName);
      loginData.put("password",password);
      String convertJson = gson.toJson(loginData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      loginData = dbConnService.selectOne("storeLogin",data) ;
      if(Objects.equals(loginData.get("password").toString(), new PasswordCryptConverter().convertToDatabaseColumn(password))){
        String token = new JwtProvider().jwtCreater(
            0,
            0,
            Integer.parseInt(loginData.get("idx").toString()),
                0
        );
        rtnVal.put("token", token);
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
  public HashMap approveList(Integer idx) {
    String error = null;
    HashMap idxData = new HashMap<>();
    List<HashMap> listData = new ArrayList();
    idxData.put("storeIdx" , idx);
    try {

      String convertJson = gson.toJson(idxData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      listData = dbConnService.select("storeApproveFind", data);

      for(int i=0; i < listData.size(); i++) {
        if(listData.get(i).get("approvedDate") != null) {
          String approvedDate = new SimpleDateFormat("yyyy-MM-dd").format(listData.get(i).get("approvedDate"));
          listData.get(i).put("approvedDate", approvedDate);
        }

        if(listData.get(i).get("registeredDate") != null) {
          String registeredDate = new SimpleDateFormat("yyyy-MM-dd").format(listData.get(i).get("registeredDate"));
          listData.get(i).put("registeredDate", registeredDate);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      error = "조회실패";
    }
    if (error != null) {
      rtnVal.put("result", false);
      rtnVal.put("token", null);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", listData);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap approveUpdate(Integer idx) {
    String error = null;
    HashMap idxData = new HashMap<>();
    List<HashMap> listData = new ArrayList();
    idxData.put("idx" , idx);
    try {

      String convertJson = gson.toJson(idxData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      dbConnService.update("storeApproveUpdate", data);

//      if(notice.equals("프로필")) {
//        dbConnService.update("infoApproveUpdate", data);
//      }

    } catch (Exception e) {
      e.printStackTrace();
      error = "업데이트 실패";
    }
    if (error != null) {
      rtnVal.put("result", false);
      rtnVal.put("token", null);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", listData);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap storeCoachCount(Integer idx) {
    String error = null;
    HashMap idxData = new HashMap<>();
    HashMap result = new HashMap<>();
    List<HashMap> listData = new ArrayList();
    idxData.put("storeIdx" , idx);
    try {
      String convertJson = gson.toJson(idxData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);
      result =  dbConnService.selectOne("storeCoachCount", data);

    } catch (Exception e) {
      e.printStackTrace();
      error = "업데이트 실패";
    }
    if (error != null) {
      rtnVal.put("result", false);
      rtnVal.put("token", null);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", result);
    }
    rtnVal.put("errorMsg", error);
    return rtnVal;
  }

  @Override
  public HashMap storeName(Integer idx) {
    String error = null;

    HashMap idxData = new HashMap();
    HashMap result = new HashMap();
    idxData.put("storeIdx", idx);

    try {
      String convertJson = gson.toJson(idxData);
      HashMap data = gson.fromJson(convertJson, HashMap.class);

      result = dbConnService.selectOne("getStoreName", data);
    } catch (Exception e) {
      e.printStackTrace();
      error = "조회 실패";
    }

    if(error != null) {
      rtnVal.put("result", false);
      rtnVal.put("token", null);
    } else {
      rtnVal.put("result", true);
      rtnVal.put("data", result);
    }

    rtnVal.put("errorMsg", error);

    return rtnVal;
  }

}
