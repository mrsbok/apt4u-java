package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class CenterApproveForm {

        @ApiModelProperty(
            name = "대상 식별 인덱스"
            , value = "타겟 Idx"
            , required = true
        )

        Integer targetIdx ;


        @ApiModelProperty(
            name = "소속센터"
            , value = "센터idx"
        )
        Integer affilatedCenter;

        @ApiModelProperty(
            name = "approvalStatus"
            , value = "승인요청상태"
            , required = true
        )
        String approvalStatus;

        @ApiModelProperty(
            name = "notice"
            , value = "인증 요청 항목"
            , example = "프로필"
            , required = true
        )
        String notice;


        Integer trainerIdx;
}
