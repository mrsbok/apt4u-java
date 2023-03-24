package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 상세 정보 입력 폼")
@Data
public class PtTrainerProfileForm {

        Integer idx = null;

        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        // 소개
        @ApiModelProperty(
            value = "트레이너 소개 글",
            example = "안녕하세요 저는...,죄송합니다.",
            required = true
        )
        String introduction;

        @ApiModelProperty(
            value = "수상 경력",
            example = "0",
            required = true,
            dataType = "int"
        )
        Byte awardWinning;

        @ApiModelProperty(
            value = "근무 경력",
            example = "0",
            required = true,
            dataType = "int"

        )
        Byte workExperience;

        @ApiModelProperty(
            value = "자격 사항",
            example = "0",
            required = true,
            dataType = "int"
        )
        Byte qualification;

        @ApiModelProperty(
            value = "전문 분야",
            example = "#바른체형,#허리운동",
            required = true
        )
        String specialty;


}
