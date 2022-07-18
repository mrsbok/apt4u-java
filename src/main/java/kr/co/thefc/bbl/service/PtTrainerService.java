package kr.co.thefc.bbl.service;

import kr.co.thefc.bbl.model.trainerForm.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    HashMap centerApprovedSave(Integer idx,CenterApproveForm centerApproveForm);

    HashMap buyInformtaionSave(List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm);
    HashMap feeInformationSave(PtFeeInformationDetailForm ptFeeInformationDetailForm);
    HashMap feeInformationSelect(Integer ptTrainerIdx);

    HashMap profileSave(Integer ptTrainerIdx, List<MultipartFile> request);
    HashMap workExperienceSave(PtTrainerWorkExperienceForm ptTrainerWorkExperienceFormList
        ,List<MultipartFile> request);

    HashMap workExperienceSelect(Integer idx);

    HashMap awardWinningSelect(Integer idx);

    HashMap qualitificationSelect(Integer idx);

    HashMap awardWinningSave(PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList
        ,List<MultipartFile> request);

    HashMap qualitificationSave(PTtrainersQualitificationForm pTtrainersQualitificationFormList
        ,List<MultipartFile> request);

    HashMap qualitificationDelete(DeleteQualitificationForm deleteQualitificationForm);

    HashMap ptTrainersScheduleSave(PTScheduleForm ptScheduleForm);
    HashMap lessionSave(PTLessionForm ptLessionForm);
    HashMap userPtRecordSave(UserPtRecordForm UserPtRecordForm);
    HashMap getSchedule(UserPtRecordForm UserPtRecordForm);
    HashMap userPtRecordSelect(UserPtRecordForm UserPtRecordForm);
    HashMap updateRecords(UserPtRecordForm UserPtRecordForm);

    HashMap ptTrainersScheduleSelect(PTScheduleForm ptScheduleForm);
    HashMap updateSchedule(PTScheduleForm ptScheduleForm);
    HashMap login(String userName,String password);
    HashMap transactionSelect(TransactionForm transactionForm);

     HashMap emailCheck(String userName);
     HashMap deleteAccount(DeleteTrainerForm deleteTrainerForm);
     HashMap completePayment(Integer transactionIdx, Integer trainerIdx);
     HashMap approveCenterSelect(Integer trainerIdx);
}