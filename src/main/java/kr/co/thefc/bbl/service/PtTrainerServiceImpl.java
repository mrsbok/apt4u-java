package kr.co.thefc.bbl.service;


import com.google.gson.Gson;
import kr.co.thefc.bbl.converter.PasswordCryptConverter;
import kr.co.thefc.bbl.model.trainerForm.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PtTrainerServiceImpl implements PtTrainerService {

//    private final PtTrainerRepository ptTrainerRepository;
////
//  private final PtTrainerInfoBasicRepository ptTrainerInfoBasicRepository;
//  private final PtTrainerAuthenticationRepository ptTrainerAPtTrainerInfoBasicRepository;
//
//
//  private final TrainerMapper trainerMapper;
    private Gson gson =  new Gson();
    HashMap rtnVal = new HashMap();

    @Autowired
    private DBConnService dbConnService;

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
}