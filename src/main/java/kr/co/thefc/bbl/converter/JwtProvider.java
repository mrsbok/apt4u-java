package kr.co.thefc.bbl.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtProvider {

	public String tokenKey  = "bbl";
	public String jwtCreater(Integer idx){
		//토큰 생성시간
		LocalDateTime createDate = LocalDateTime.now();

		//토큰 마감시간
		LocalDateTime endDate = createDate.plusSeconds(60000000);

		//토큰 내용
		Map<String,Object> claims = Map.of(
				"idx" , idx,
				"createdDate" , createDate.format(DateTimeFormatter.ISO_DATE_TIME)
		);


		//토큰생성
		return Jwts
				.builder()
				.setClaims(claims)
				.signWith(SignatureAlgorithm.HS512, tokenKey.getBytes())
				.compact();
	}

}
