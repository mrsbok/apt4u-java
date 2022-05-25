package kr.co.thefc.bbl.controller.api.trainer;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import kr.co.thefc.bbl.model.trainerForm.*;
import kr.co.thefc.bbl.service.PtTrainerService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api")
@Api(description = "트레이너 정보 관리")
public class ptTrainerController {
	Logger log;


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
			@RequestBody PtTrainerDetailForm ptTrainerDetailForm)   {
		return ptTrainerService.trainerInfoDetailSave(ptTrainerDetailForm);
	}

	@ApiOperation(value = "트레이너 정보 조회",notes = "트레이너 정보 조회")
	@PostMapping("trainer/select-information")
	public HashMap selectInformation(
			@RequestParam(value = "idx") Integer idx)   {
		return ptTrainerService.selectInformation(idx);
	}


	@ApiOperation(value = "트레이너 기본정보 수정",notes = "트레이너 기본정보 수정")
	@PostMapping("trainer/update-information")
	public HashMap updateInformation(
			@RequestBody PtTrainerForm ptTrainerForm)   {
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

}
