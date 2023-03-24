package io.apt4u.main.service;

import com.google.gson.Gson;
import io.apt4u.main.converter.JwtProvider;
import io.apt4u.main.converter.PasswordCryptConverter;
import io.apt4u.main.model.adminForm.AdminForm;
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
public class AdminServiceImpl implements AdminService{
    private Gson gson = new Gson();
    HashMap rtnVal = new HashMap();

    @Autowired
    private DBConnService dbConnService;

    @Autowired
    private S3Service s3Service;

    @Override
    public HashMap adminRegister(AdminForm adminForm) {
        String error = null;

        try {
            adminForm.setPassword(
                    new PasswordCryptConverter().convertToDatabaseColumn(adminForm.getPassword())
            );
            String convertJSon = gson.toJson(adminForm);
            HashMap data = gson.fromJson(convertJSon, HashMap.class);

            dbConnService.insert("adminSave", data);
            dbConnService.insert("adminInfoSave", data);
            dbConnService.insert("adminAuthenticateSave", data);
        } catch (Exception e) {
            e.printStackTrace();
            error = "데이터 저장 실패.";
        }

        if(error != null) {
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
        HashMap idData = new HashMap();

        try {
            idData.put("username", userName);
            String convertJSon = gson.toJson(idData);

            HashMap data = gson.fromJson(convertJSon, HashMap.class);

            HashMap flag = dbConnService.selectOne("adminCheckId", data);

            if(flag == null) {
                message = "사용 가능한 아이디입니다.";
                rtnVal.put("flag", true);
                rtnVal.put("message", message);
            } else {
                message = "중복된 아이디입니다.";
                rtnVal.put("flag", false);
                rtnVal.put("message", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "데이터를 조회하지 못했습니다.";
        }

        if (error != null) {
          rtnVal.put("result", false);
          rtnVal.put("token", null);
        } else {
          rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @Override
    public HashMap login(String userName, String password) {
        String error = null;
        HashMap loginData = new HashMap();

        try {
            loginData.put("userName", userName);
            loginData.put("password", password);

            String convertJson = gson.toJson(loginData);

            HashMap data = gson.fromJson(convertJson, HashMap.class);

            loginData = dbConnService.selectOne("adminLogin", data);

            if(Objects.equals(loginData.get("password").toString(), new PasswordCryptConverter().convertToDatabaseColumn(password))) {
                String token = new JwtProvider().jwtCreater(
                        0, 0, 0,
                        Integer.parseInt(loginData.get("idx").toString())
                );

                dbConnService.update("adminLastAccess", loginData);

                rtnVal.put("token", token);
            } else {
                error = "로그인 실패. 패스워드 또는 ID를 확인하여 주십시오";
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = "로그인 실패. 패스워드 또는 ID를 확인하여 주십시오";
        }

        if (error != null) {
          rtnVal.put("result", false);
          rtnVal.put("token", null);
        } else {
          rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }

    @Override
    public HashMap approveList() {
        String error = null;
        List<HashMap> listData = new ArrayList<>();

        try {
            listData = dbConnService.selectWithoutParam("adminApproveFind");

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
            error = "조회 실패";
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
        HashMap statusData = new HashMap();

        statusData.put("storeIdx", idx);

        try{
            String converJson = gson.toJson(statusData);
            HashMap data = gson.fromJson(converJson, HashMap.class);
            dbConnService.update("adminApproveUpdate", data);
        } catch (Exception e) {
            e.printStackTrace();
            error = "업데이트 실패";
        }

        if (error != null) {
          rtnVal.put("result", false);
          rtnVal.put("token", null);
        } else {
          rtnVal.put("result", true);
        }
        rtnVal.put("errorMsg", error);

        return rtnVal;
    }
}
