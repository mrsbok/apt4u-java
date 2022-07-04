package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "경력 폼")
@Data
public class  PtTrainerWorkExperienceForm {

        Integer idx = null;

        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        // 소개
        @ApiModelProperty(
            value = "근무 시작일 (0000년 00월)",
            example = "2022-01-01",
            required = true
        )
        String startDate;

        @ApiModelProperty(
            value = "근무 종료일 (0000년 00월)",
            example = "2022-03-31",
            required = true
        )
        String endDate;

        @ApiModelProperty(
            value = "근무 기관명",
            example = "우리집",
            required = true

        )
        String organization;

        @ApiModelProperty(
            value = "담당 업무 및 직책",
            example = "브레이크 댄스",
            required = true
        )
        String role;

}
