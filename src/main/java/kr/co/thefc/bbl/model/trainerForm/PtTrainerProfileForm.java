package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 상세 정보 입력 폼")
@Data
public class PtTrainerProfileForm {

        Integer idx = null;

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

}
