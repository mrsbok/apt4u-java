package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class UserPtRecordForm extends UserPtContentsForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        @ApiModelProperty(
            value = "PT 시작 시각",
           example = "01:00:00",
            required = true
        )
        String startTime;

        @ApiModelProperty(
            value = "PT 일자",
           example = "2022-01-05",
            required = true
        )
        String date;


        @ApiModelProperty(
            value = "PT 종료 시각",
           example = "02:00:00",
            required = true
        )
        String endTime;

        @ApiModelProperty(
            value = "PT 운동 종류",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte exerciseCount;


        @ApiModelProperty(
            value = "PT 기록 작성자 구분",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte recordedBy;


        @ApiModelProperty(
            value = "유저 idx",
            example = "1",
            required = true
        )
        Integer userIdx;
}
