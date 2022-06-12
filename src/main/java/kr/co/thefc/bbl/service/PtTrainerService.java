package kr.co.thefc.bbl.service;

import kr.co.thefc.bbl.model.trainerForm.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.List;

@Service
public interface PtTrainerService {

    HashMap trainerSave(PtTrainerForm ptTrainerForm);

    HashMap trainerInfoDetailSave(PtTrainerProfileForm ptTrainerProfileForm);

    HashMap oneDayAmountSave(OneDayAmountForm oneDayAmountForm);

    HashMap selectInformation(Integer idx);

    HashMap selectDetailInformation(Integer idx);
    HashMap updateInformation(PtTrainerForm ptTrainerForm);
    HashMap updatePassword(Integer idx,String password);
    HashMap centerApprovedSave(Integer idx,Integer AffiliateCenter,String ApprovalStatus);

    HashMap buyInformtaionSave(List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm);
    HashMap feeInformationSave(PtFeeInformationDetailForm ptFeeInformationDetailForm);
    HashMap feeInformationSelect(Integer ptTrainerIdx);

    HashMap profileSave(Integer ptTrainerIdx, MultipartHttpServletRequest request);
    HashMap workExperienceSave(PtTrainerWorkExperienceForm ptTrainerWorkExperienceFormList
        ,MultipartHttpServletRequest request);

    HashMap awardWinningSave(PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList
        ,MultipartHttpServletRequest request);

    HashMap qualitificationSave(PTtrainersQualitificationForm pTtrainersQualitificationFormList
        ,MultipartHttpServletRequest request);

    HashMap qualitificationDelete(DeleteQualitificationForm deleteQualitificationForm);

    HashMap ptTrainersScheduleSave(PTScheduleForm ptScheduleForm);
    HashMap lessionSave(PTLessionForm ptLessionForm);
    HashMap userPtRecordSave(UserPtRecordForm UserPtRecordForm);
}