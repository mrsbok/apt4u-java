package kr.co.thefc.bbl.model.storeForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 가입 정보 폼 입니다.")
@Data
public class StoreForm {

        Integer idx = null;

        // 업체 ID
        @ApiModelProperty(
            value = "업체 ID",
            example = "asd123",
            required = true
        )
        String id;

        // 업체 패스워드
        @ApiModelProperty(
            value = "업체 패스워드",
            example = "12345"
        )
        String password;


        @ApiModelProperty(
            value = "업체명",
            example = "30년전통 이태리 돼지국밥",
            required = true
        )
        String storeName;

        @ApiModelProperty(
            value = "사업자등록번호",
            example = "1111111111",
            required = true
        )
        String corpRegNumber;


        @ApiModelProperty(
            value = "대표자명",
            example = "방국봉",
            required = true
        )
        String presidentName;

        @ApiModelProperty(
            value = "업태",
            example = "1",
            dataType = "int",
            required = true
        )
        Byte bizType;

        @ApiModelProperty(
            value = "업종",
            example = "운동",
            required = true
        )
        String bizTopic;

        @ApiModelProperty(
            value = "우편번호",
            example = "12345",
            required = true
        )
        String  zipCode;

        @ApiModelProperty(
            value = "주소",
            example = "00시 00도 00 군",
            required = true
        )
        String address;

        @ApiModelProperty(
            value = "서비스 지역 (시, 도)",
            example = "111",
            dataType = "int",
            required = true
        )
        Byte serviceRegion;

        @ApiModelProperty(
            value = "서비스 지역 (구, 군)",
            example = "222",
            dataType = "int",
            required = true
        )
        Byte serviceLocalArea;

        @ApiModelProperty(
            value = "대표 전화번호",
            example = "010-1234-5678",
            required = true
        )
        String mainTelephone;

        @ApiModelProperty(
            value = "담당자명",
            example = "남남수",
            required = true
        )
        String personInCharge;

        @ApiModelProperty(
            value = "담당자 연락처",
            example = "010-2345-5678",
            required = true
        )
        String contactNumber;

        @ApiModelProperty(
            value = "입점 분야",
            example = "1",
            dataType = "int",
            required = true
        )
        Byte storeType;

        @ApiModelProperty(
            value = "입점 승인 상태",
            example = "1",
            dataType = "int"
        )
        Byte approvalStatus;

        @ApiModelProperty(
            value = "입점 신청일",
            example = "2022-06-01"
        )
        String registeredDate;
}
