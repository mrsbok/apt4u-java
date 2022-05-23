package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 가입 정보 폼 입니다.")
@Data
public class PtTrainerForm {

        Integer idx = null;

        // 트레이너 ID
        @ApiModelProperty(
            value = "트레이너 ID",
            example = "asd123",
            required = true
        )
        String id;

        // 트레이너 ID
        @ApiModelProperty(
            value = "트레이너 패스워드",
            example = "12345"
        )
        String password;

        // 인증방법
        @ApiModelProperty(
            value = "인증 방법",
            example = "1",
            required = true,
            dataType = "int"

        )
        Byte certType;

        // 트레이너 이름
        @ApiModelProperty(
            value = "트레이너 이름",
            example = "김춘배",
            required = true
        )
        String ptTrainerName;

        // 트레이너 생년월일
        @ApiModelProperty(
            value = "트레이너 생년월일",
            example = "20220101",
            required = true
        )
        String brithDay;

        // 트레이너 성별
        @ApiModelProperty(
            value = "트레이너 성별",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte gender;

        // 프로그램 이름
        @ApiModelProperty(
            value = "프로그램 이름",
            example = "test"
        )
        String programName;

        // 소속 센터
        @ApiModelProperty(
            value = "소속센터",
            example = "1",
            required = true
        )
        Integer affilatedCenter;

        // 소속 센터  인증 여부
        @ApiModelProperty(
            value = "소속센터 인증 여부",
            example = "1",
            dataType = "int"
        )
        Byte approvalstatus;

        // 본인인증 여부
        @ApiModelProperty(
            value = "본인인증 여부",
            example = "1"
        )
        Boolean elDas;


        @ApiModelProperty(
            value = "휴대폰 번호",
            example = "010-1234-8765"
        )
        String phoneNumber;


        @ApiModelProperty(
            value = "예명",
            example = "가리봉동 왕쇼바"
        )
        String nickName;


}
