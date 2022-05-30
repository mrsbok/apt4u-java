package kr.co.thefc.bbl.service;

import kr.co.thefc.bbl.model.trainerForm.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public interface PtTrainerService {

    HashMap trainerSave(PtTrainerForm ptTrainerForm);

    HashMap trainerInfoDetailSave(PtTrainerDetailForm ptTrainerDetailForm);

    HashMap oneDayAmountSave(OneDayAmountForm oneDayAmountForm);

    HashMap selectInformation(Integer idx);
    HashMap updateInformation(PtTrainerForm ptTrainerForm);
    HashMap updatePassword(Integer idx,String password);
    HashMap centerApprovedSave(Integer idx,Integer AffiliateCenter,String ApprovalStatus);

    HashMap buyInformtaionSave(List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm);
    HashMap feeInformationSave(PtFeeInformationDetailForm ptFeeInformationDetailForm);
    List<HashMap> feeInformationSelect(Integer ptTrainerIdx);
}