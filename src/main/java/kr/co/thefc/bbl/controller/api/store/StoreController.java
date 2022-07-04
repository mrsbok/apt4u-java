package kr.co.thefc.bbl.controller.api.store;


import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.model.storeForm.StoreForm;
import kr.co.thefc.bbl.service.PtTrainerService;
import kr.co.thefc.bbl.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@RequestMapping("api")
@Api(description = "업체 관리 \t\t\" 로그인 기능요청 -> autho+rize 버튼 클릭후 로그인 후 받은 토근 입력 -> 하단의 기능요청 순서로 진행\"")
public class StoreController {

	@Autowired
	public StoreService storeService;

	@ApiOperation(
			value = "업체 회원가입"
			, notes = "업체 회원가입")
	@PostMapping("store/register")
	public HashMap storeRegister(
			@RequestBody StoreForm storeForm
			)  {
		return storeService.storeRegister(storeForm);
	}
	@ApiOperation(
			value = "이메일 중복확인"
			, notes = "이메일 중복확인" +
			"asd1237")
	@PostMapping("store/email-check")
	public HashMap emailcheck(@RequestParam String userName)  {
		return storeService.emailCheck(userName);
	}

	@ApiOperation(
			value = "업체 로그인"
			, notes = "업체 로그인" +
			"asd1237")
	@PostMapping("store/login")
	public HashMap login(
			@RequestParam String userName,
			@RequestParam String password
	)  {
		return storeService.login(userName,password);
	}

	@ApiOperation(
			value = "트레이너 승인"
			, notes = "트레이너 승인")
	@PostMapping("store/trainer-approve")
	public HashMap trainerApprove(
			@RequestParam String userName,
			@RequestParam String password
	)  {
		return storeService.login(userName,password);
	}


}
