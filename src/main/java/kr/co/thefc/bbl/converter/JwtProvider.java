package kr.co.thefc.bbl.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.models.auth.In;

import javax.lang.model.type.NullType;

public class JwtProvider {

	public String tokenKey  = "bbl";
	public String jwtCreater(Integer trainerIdx, Integer userIdx){
		//토큰 생성시간
		LocalDateTime createDate = LocalDateTime.now();

		//토큰 마감시간
		LocalDateTime endDate = createDate.plusSeconds(60000000);

		//토큰 내용
		Map<String,Object> claims = Map.of(
				"trainerIdx" , trainerIdx,
				"userIdx" , userIdx,
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
