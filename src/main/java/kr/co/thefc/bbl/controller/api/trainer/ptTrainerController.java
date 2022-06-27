package kr.co.thefc.bbl.controller.api.trainer;


import com.amazonaws.services.s3.model.S3Object;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.model.trainerForm.*;
import kr.co.thefc.bbl.service.PtTrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api")
@Api(description = "트레이너 정보 관리 \t\t\"트레이너 로그인 기능요청 -> autho+rize 버튼 클릭후 로그인 후 받은 토근 입력 -> 하단의 기능요청 순서로 진행\"")


public class ptTrainerController {

	@Autowired
	public PtTrainerService ptTrainerService;

	@ApiOperation(value = "트레이너 등록",notes = "트레이너 등록")
	@PostMapping("trainer/register")
	public HashMap ptRegister(
			@RequestBody PtTrainerForm ptTrainerForm)  {
		return ptTrainerService.trainerSave(ptTrainerForm);
	}


	@ApiOperation(value = "트레이너 상세정보 등록",notes = "트레이너 상세정보 등록")
	@PostMapping("trainer/proflie-register")
	public HashMap ptTrainerProfileRegister(
			@RequestBody PtTrainerProfileForm ptTrainerProfileForm,HttpServletRequest request)   {
		String token = request.getHeader("token");
		int result = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptTrainerProfileForm.setTarinerIdx(result);
		return ptTrainerService.trainerInfoDetailSave(ptTrainerProfileForm);
	}

	@ApiOperation(value = "트레이너 정보 조회",notes = "트레이너 정보 조회")
	@PostMapping("trainer/select-information")
	public HashMap selectInformation(HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		return ptTrainerService.selectInformation(idx);
	}
	@ApiOperation(value = "트레이너 상세 정보 조회",notes = "트레이너 상세 정보 조회")
	@PostMapping("trainer/select-detail-information")
	public HashMap selectDetailInformation(
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		return ptTrainerService.selectDetailInformation(idx);
	}

	@ApiOperation(value = "트레이너 기본정보 수정",notes = "트레이너 기본정보 수정")
	@PostMapping("trainer/update-information")
	public HashMap updateInformation(
			@RequestBody PtTrainerForm ptTrainerForm,	HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptTrainerForm.setIdx(idx);
		return ptTrainerService.updateInformation(ptTrainerForm);
	}


	@ApiOperation(value = "1회 체험가 설정",notes = "1회 체험가 설정")
	@PostMapping("trainer/one-day-amount")
	public HashMap configurationOneDayAmount(
			@RequestBody OneDayAmountForm oneDayAmountForm,	HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));

		return ptTrainerService.oneDayAmountSave(oneDayAmountForm);
	}

	@ApiOperation(
			value = "패스워드 변경"
			, notes = "패스워드 변경")
	@ApiImplicitParams(
			{
					@ApiImplicitParam(
							name = "password"
							, value = "비밀번호"
							, required = true
					)
			}
	)
	@PostMapping("trainer/update-password")
	public HashMap passwordUpdate(
			@RequestParam String password,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		return ptTrainerService.updatePassword(idx,password);
	}
	@ApiOperation(
			value = "센터 승인 요청"
			, notes = "센터 승인 요청")
	@ApiImplicitParams(
			{
					@ApiImplicitParam(
							name = "affilatedCenter"
							, value = "센터idx"
							, required = true
					),
					@ApiImplicitParam(
							name = "approvalStatus"
							, value = "승인요청상태"
							, required = true
					)
			}
	)
	@PostMapping("trainer/center-approved")
	public HashMap ptTrainerCenterApproved(
			@RequestParam Integer affilatedCenter,
			@RequestParam String approvalStatus,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		return ptTrainerService.centerApprovedSave(idx,affilatedCenter,approvalStatus);
	}


	@ApiOperation(
			value = "구매 정보 등록"
			, notes = "구매 정보 등록")
	@PostMapping("trainer/buy-information")
	public HashMap ptTrainerBuyInformation(
			@RequestBody List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptTrainerBuyInformationForm.forEach(Data -> Data.setIdx(idx));
		return ptTrainerService.buyInformtaionSave(ptTrainerBuyInformationForm);
	}

	@ApiOperation(
			value = "요금 정보 등록"
			, notes = "요금 정보 등록")
	@PostMapping("trainer/fee-information")
	public HashMap ptFeeInformation(
			@RequestBody PtFeeInformationDetailForm ptFeeInformationDetailForm,	HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptFeeInformationDetailForm.ptFeeInformtaionForms.setIdx(idx);
		return ptTrainerService.feeInformationSave(ptFeeInformationDetailForm);
	}

	@ApiOperation(
			value = "요금 정보 조회"
			, notes = "로그인 후 토큰 필요")
	@PostMapping("trainer/fee-information-select")
	public HashMap ptFeeInformationSelect(
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));

		return ptTrainerService.feeInformationSelect(idx);
	}

	@ApiOperation(
			value = "프로필 저장"
			, notes = "프로필 저장(토큰 전송)")
	@PostMapping("trainer/profile-save")
	public HashMap ptProfileSave(
			MultipartHttpServletRequest request)  {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		return ptTrainerService.profileSave(idx,request);

		}

	@ApiOperation(
			value = "근무경력 저장"
			, notes = "근무경력 저장")
	@PostMapping("trainer/work-experience-save")
	public HashMap ptTrainerWorkExperience(
			@RequestPart PtTrainerWorkExperienceForm ptTrainerWorkExperienceFormList,
			MultipartHttpServletRequest request)  {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptTrainerWorkExperienceFormList.setTarinerIdx(idx);
		return ptTrainerService.workExperienceSave(ptTrainerWorkExperienceFormList,request);

	}

	@ApiOperation(
			value = "수상내역 저장"
			, notes = "수상내역 저장")
	@PostMapping("trainer/award-winning-save")
	public HashMap ptTrainerAwardWinning(
			@RequestPart PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList,
			MultipartHttpServletRequest request)  {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		pTtrainersAwardWinningFormsList.setTarinerIdx(idx);
		return ptTrainerService.awardWinningSave(pTtrainersAwardWinningFormsList,request);

	}

	@ApiOperation(
			value = "자격증 등록"
			, notes = "자격증 등록")
	@PostMapping("trainer/qualitification-save")
	public HashMap ptTrainersQualitification(
			@RequestPart PTtrainersQualitificationForm pTtrainersQualitificationFormList,
			MultipartHttpServletRequest request)  {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		pTtrainersQualitificationFormList.setTarinerIdx(idx);
		return ptTrainerService.qualitificationSave(pTtrainersQualitificationFormList,request);
	}

	@ApiOperation(
			value = "자격사항 삭제"
			, notes = "자격사항 삭제")
	@PostMapping("trainer/delete-qualifications")
	public HashMap deleteQualitification(
			@RequestBody DeleteQualitificationForm deleteQualitificationForm)  {
		return ptTrainerService.qualitificationDelete(deleteQualitificationForm);
	}

	@ApiOperation(
			value = "일정 등록"
			, notes = "일정 등록")
	@PostMapping("trainer/pt-schedule")
	public HashMap ptTrainersSchedule(
			@RequestBody PTScheduleForm ptScheduleForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptScheduleForm.setTarinerIdx(idx);
		return ptTrainerService.ptTrainersScheduleSave(ptScheduleForm);
	}

		@ApiOperation(
			value = "피티 레슨 저장"
			, notes = "피티 레슨 저장")
	@PostMapping("trainer/reason-save")
	public HashMap ptTrainersPTUsers(
			@RequestBody PTLessionForm ptLessionForm,
			HttpServletRequest request)   {
			String token = request.getHeader("token");
			int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
			ptLessionForm.setTarinerIdx(idx);
			return ptTrainerService.lessionSave(ptLessionForm);
	}


	@ApiOperation(
			value = "피티 레슨 기록 저장"
			, notes = "피티 레슨 기록 저장")
	@PostMapping("trainer/record-save")
	public HashMap PTUsersRecords(
			@RequestBody UserPtRecordForm UserPtRecordForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		UserPtRecordForm.setTarinerIdx(idx);
		return ptTrainerService.userPtRecordSave(UserPtRecordForm);
	}

	@ApiOperation(
			value = "피티 레슨 기록 조회"
			, notes = "피티 레슨 기록 조회")
	@PostMapping("trainer/select-records")
	public HashMap PTUsersRecordsSelect(
			@RequestBody UserPtRecordForm UserPtRecordForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		UserPtRecordForm.setTarinerIdx(idx);
		return ptTrainerService.userPtRecordSelect(UserPtRecordForm);
	}

	@ApiOperation(
			value = "일정 조회"
			, notes = "일정 조회")
	@PostMapping("trainer/select-Schedule")
	public HashMap SelectSchedule(
			@RequestBody UserPtRecordForm UserPtRecordForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		UserPtRecordForm.setTarinerIdx(idx);
		return ptTrainerService.getSchedule(UserPtRecordForm);
	}
	@ApiOperation(
			value = "일정 수정"
			, notes = "일정 수정")
	@PostMapping("trainer/update-Schedule")
	public HashMap PTUsersUpdateSchedule(
			@RequestBody PTScheduleForm ptScheduleForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		ptScheduleForm.setTarinerIdx(idx);
		return ptTrainerService.updateSchedule(ptScheduleForm);
	}

	@ApiOperation(
			value = "운동 수정"
			, notes = "운동 수정")
	@PostMapping("trainer/update-records")
	public HashMap PTUsersUpdateRecords(
			@RequestBody UserPtRecordForm UserPtRecordForm,
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		UserPtRecordForm.setTarinerIdx(idx);
		return ptTrainerService.updateRecords(UserPtRecordForm);
	}


	@ApiOperation(
			value = "트레이너 로그인"
			, notes = "로그인시 토큰 발급, swagger 상단의 Authorize버튼 클릭후 토큰 입력 ")
	@PostMapping("trainer/login")
	public HashMap PTtrainerLogin(
			@RequestParam String userName,
			@RequestParam String password
			)  {
		return ptTrainerService.login(userName,password);
	}


	@ApiOperation(
			value = "회원 거래내역 조회"
			, notes = "회원 거래내역 조회")
	@PostMapping("trainer/transaction-select")
	public HashMap TransactionSelect(TransactionForm transactionForm,HttpServletRequest request)  {
		String token = request.getHeader("token");
		int result = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("trainerIdx")));
		transactionForm.setTrainerIdx(result);
		return ptTrainerService.transactionSelect(transactionForm);
	}

}
