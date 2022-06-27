package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 회원 운동 기록 폼")
@Data
public class PTLessionForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        @ApiModelProperty(
            value = "상품 idx",
            example = "1",
            required = true
        )
        Integer productIdx;

        @ApiModelProperty(
            value = "PT 시작일",
           example = "2022-01-01",
            required = true
        )
        String dateStart;

        @ApiModelProperty(
            value = "PT 종료일",
           example = "2022-01-05",
            required = true
        )
        String dateEnd;


        @ApiModelProperty(
            value = "PT 고객의 확인 등록일",
           example = "2022-01-01",
            required = true
        )
        String approvedDate;

        @ApiModelProperty(
            value = "PT 이용권 잔여 횟수",
            example = "0",
            required = true,
            dataType = "int"
        )
        Byte voucher;


        @ApiModelProperty(
            value = "PT 고객의 등록 확인 여부",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte userApproved;


        @ApiModelProperty(
            value = "유저 idx",
            example = "1",
            required = true
        )
        Integer userIdx;
}
