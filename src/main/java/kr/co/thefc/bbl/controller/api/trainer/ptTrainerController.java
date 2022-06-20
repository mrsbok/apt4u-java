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

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api")
@Api(description = "트레이너 정보 관리")
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
		int result = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("idx")));
		ptTrainerProfileForm.setTarinerIdx(result);
		return ptTrainerService.trainerInfoDetailSave(ptTrainerProfileForm);
	}

	@ApiOperation(value = "트레이너 정보 조회",notes = "트레이너 정보 조회")
	@PostMapping("trainer/select-information")
	public HashMap selectInformation(HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("idx")));
		return ptTrainerService.selectInformation(idx);
	}
	@ApiOperation(value = "트레이너 상세 정보 조회",notes = "트레이너 상세 정보 조회")
	@PostMapping("trainer/select-detail-information")
	public HashMap selectDetailInformation(
			HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("idx")));
		return ptTrainerService.selectDetailInformation(idx);
	}

	@ApiOperation(value = "트레이너 기본정보 수정",notes = "트레이너 기본정보 수정")
	@PostMapping("trainer/update-information")
	public HashMap updateInformation(
			@RequestBody PtTrainerForm ptTrainerForm,	HttpServletRequest request)   {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("idx")));
		ptTrainerForm.setIdx(idx);
		System.out.println();
		return ptTrainerService.updateInformation(ptTrainerForm);
	}


	@ApiOperation(value = "1회 체험가 설정",notes = "1회 체험가 설정")
	@PostMapping("trainer/one-day-amount")
	public HashMap configurationOneDayAmount(
			@RequestBody OneDayAmountForm oneDayAmountForm)   {
		return ptTrainerService.oneDayAmountSave(oneDayAmountForm);
	}

	@ApiOperation(
			value = "패스워드 변경"
			, notes = "패스워드 변경")
	@ApiImplicitParams(
			{
					@ApiImplicitParam(
							name = "idx"
							, value = "트레이너 idx"
							, required = true
					)
					,
					@ApiImplicitParam(
							name = "password"
							, value = "비밀번호"
							, required = true
					)
			}
	)
	@PostMapping("trainer/update-password")
	public HashMap passwordUpdate(
			@RequestParam Integer idx,
			@RequestParam String password)  {
		return ptTrainerService.updatePassword(idx,password);
	}
	@ApiOperation(
			value = "센터 승인 요청"
			, notes = "센터 승인 요청")
	@ApiImplicitParams(
			{
					@ApiImplicitParam(
							name = "idx"
							, value = "트레이너 idx"
							, required = true
					)
					,
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
			@RequestParam Integer idx,
			@RequestParam Integer affilatedCenter,
			@RequestParam String approvalStatus)  {
		return ptTrainerService.centerApprovedSave(idx,affilatedCenter,approvalStatus);
	}


	@ApiOperation(
			value = "구매 정보 등록"
			, notes = "구매 정보 등록")
	@PostMapping("trainer/buy-information")
	public HashMap ptTrainerBuyInformation(
			@RequestBody List<PtTrainerBuyInformationForm> ptTrainerBuyInformationForm)  {
		return ptTrainerService.buyInformtaionSave(ptTrainerBuyInformationForm);
	}

	@ApiOperation(
			value = "요금 정보 등록"
			, notes = "요금 정보 등록")
	@PostMapping("trainer/fee-information")
	public HashMap ptFeeInformation(
			@RequestBody PtFeeInformationDetailForm ptFeeInformationDetailForm)  {
		return ptTrainerService.feeInformationSave(ptFeeInformationDetailForm);
	}

	@ApiOperation(
			value = "요금 정보 조회"
			, notes = "요금 정보 조회")
	@ApiImplicitParams(
			{
					@ApiImplicitParam(
							name = "ptTrainerIdx"
							, value = "요금정보 idx"
							, required = true
					)

			}
	)
	@PostMapping("trainer/fee-information-select")
	public HashMap ptFeeInformationSelect(
			@RequestParam Integer ptTrainerIdx)  {
		return ptTrainerService.feeInformationSelect(ptTrainerIdx);
	}

	@ApiOperation(
			value = "프로필 저장"
			, notes = "프로필 저장")
	@PostMapping("trainer/profile-save")
	public HashMap ptProfileSave(
			@RequestParam Integer ptTrainerIdx,
			MultipartHttpServletRequest request)  {

		return ptTrainerService.profileSave(ptTrainerIdx,request);

		}

	@ApiOperation(
			value = "근무경력 저장"
			, notes = "근무경력 저장")
	@PostMapping("trainer/work-experience-save")
	public HashMap ptTrainerWorkExperience(
			@RequestPart PtTrainerWorkExperienceForm ptTrainerWorkExperienceFormList,
			MultipartHttpServletRequest request)  {
		return ptTrainerService.workExperienceSave(ptTrainerWorkExperienceFormList,request);

	}

	@ApiOperation(
			value = "수상내역 저장"
			, notes = "수상내역 저장")
	@PostMapping("trainer/award-winning-save")
	public HashMap ptTrainerAwardWinning(
			@RequestPart PTtrainersAwardWinningForm pTtrainersAwardWinningFormsList,
			MultipartHttpServletRequest request)  {
		return ptTrainerService.awardWinningSave(pTtrainersAwardWinningFormsList,request);

	}

	@ApiOperation(
			value = "자격증 등록"
			, notes = "자격증 등록")
	@PostMapping("trainer/qualitification-save")
	public HashMap ptTrainersQualitification(
			@RequestPart PTtrainersQualitificationForm pTtrainersQualitificationFormList,
			MultipartHttpServletRequest request)  {
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
			value = "스케쥴링"
			, notes = "스케쥴링")
	@PostMapping("trainer/pt-schedule")
	public HashMap ptTrainersSchedule(
			@RequestBody PTScheduleForm ptScheduleForm)  {
		return ptTrainerService.ptTrainersScheduleSave(ptScheduleForm);
	}

		@ApiOperation(
			value = "피티 레슨 저장"
			, notes = "피티 레슨 저장")
	@PostMapping("trainer/reason-save")
	public HashMap ptTrainersPTUsers(
			@RequestBody PTLessionForm ptLessionForm)  {
		return ptTrainerService.lessionSave(ptLessionForm);
	}


	@ApiOperation(
			value = "피티 레슨 기록 저장"
			, notes = "피티 레슨 기록 저장")
	@PostMapping("trainer/record-save")
	public HashMap PTUsersRecords(
			@RequestBody UserPtRecordForm UserPtRecordForm)  {
		return ptTrainerService.userPtRecordSave(UserPtRecordForm);
	}

	@ApiOperation(
			value = "피티 레슨 기록 조회"
			, notes = "피티 레슨 기록 조회")
	@PostMapping("trainer/select-records")
	public HashMap PTUsersRecordsSelect(
			@RequestBody UserPtRecordForm UserPtRecordForm)  {
		return ptTrainerService.userPtRecordSelect(UserPtRecordForm);
	}

	@ApiOperation(
			value = "스케쥴 조회"
			, notes = "스케쥴 조회")
	@PostMapping("trainer/select-Schedule")
	public HashMap SelectSchedule(
			@RequestBody UserPtRecordForm UserPtRecordForm)  {
		return ptTrainerService.getSchedule(UserPtRecordForm);
	}
	@ApiOperation(
			value = "일정 수정"
			, notes = "일정 수정")
	@PostMapping("trainer/update-Schedule")
	public HashMap PTUsersUpdateSchedule(
			@RequestBody PTScheduleForm ptScheduleForm)  {
		return ptTrainerService.updateSchedule(ptScheduleForm);
	}

	@ApiOperation(
			value = "운동 수정"
			, notes = "운동 수정")
	@PostMapping("trainer/update-records")
	public HashMap PTUsersUpdateRecords(
			@RequestBody UserPtRecordForm UserPtRecordForm)  {
		return ptTrainerService.updateRecords(UserPtRecordForm);
	}


	@ApiOperation(
			value = "트레이너 로그인"
			, notes = "트레이너 로그인")
	@PostMapping("trainer/login")
	public HashMap PTtrainerLogin(
			@RequestParam String userName,
			@RequestParam String password
			)  {
		return ptTrainerService.login(userName,password);
	}

	@ApiOperation(
			value = "트레이너 로그인"
			, notes = "트레이너 로그인")
	@PostMapping("trainer/login1")
	public void PTtrainerLogin1(HttpServletRequest request)  {
		String token = request.getHeader("token");
		int result = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("idx")));

	}
}
