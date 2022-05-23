package kr.co.thefc.bbl.controller.api.trainer;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.model.trainerForm.*;
import kr.co.thefc.bbl.service.PtTrainerService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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


}
