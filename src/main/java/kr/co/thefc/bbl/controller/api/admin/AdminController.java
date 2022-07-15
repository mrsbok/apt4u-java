package kr.co.thefc.bbl.controller.api.admin;


import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.thefc.bbl.converter.JwtProvider;
import kr.co.thefc.bbl.model.adminForm.AdminForm;
import kr.co.thefc.bbl.model.storeForm.StoreForm;
import kr.co.thefc.bbl.service.AdminService;
import kr.co.thefc.bbl.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@RequestMapping("api")
@Api(description = "BBL 총괄 관리 \t\t\" 로그인 기능요청 -> autho+rize 버튼 클릭후 로그인 후 받은 토근 입력 -> 하단의 기능요청 순서로 진행\"")
public class AdminController {

	@Autowired
	public AdminService adminService;

	@ApiOperation(
			value = "총괄 회원가입"
			, notes = "총괄 회원가입")
	@PostMapping("admin/register")
	public HashMap storeRegister(
			@RequestBody AdminForm adminForm
			)  {
		return adminService.adminRegister(adminForm);
	}
	@ApiOperation(
			value = "아이디 중복확인"
			, notes = "아이디 중복확인" +
			" admin")
	@PostMapping("admin/email-check")
	public HashMap emailcheck(@RequestParam String userName)  {
		return adminService.emailCheck(userName);
	}

	@ApiOperation(
			value = "총괄 로그인"
			, notes = "총괄 로그인" +
			" admin")
	@PostMapping("admin/login")
	public HashMap login(
			@RequestParam String userName,
			@RequestParam String password
	)  {
		return adminService.login(userName,password);
	}

	@ApiOperation(
			value = "업체 승인요청 조회"
			, notes = "업체 승인요청 조회")
	@PostMapping("admin/store-approve-select")
	public HashMap storeApproveList(
			HttpServletRequest request
	)  {
		String token = request.getHeader("token");
		int idx = Integer.parseInt(String.valueOf(Jwts.parser().setSigningKey(new JwtProvider().tokenKey.getBytes()).parseClaimsJws(token).getBody().get("storeIdx")));

		return adminService.approveList();
	}

	@ApiOperation(
			value = "업체 승인처리"
			, notes = "업체 승인처리")
	@PostMapping("admin/approve")
	public HashMap storeApprove(
			HttpServletRequest request,
			@RequestParam Integer storeIdx
	)  {
		String token = request.getHeader("token");

		return adminService.approveUpdate(storeIdx);
	}
}
