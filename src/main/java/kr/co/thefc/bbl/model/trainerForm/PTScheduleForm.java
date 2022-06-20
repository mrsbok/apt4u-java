package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class PTScheduleForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        @ApiModelProperty(
            value = "PT 일자",
           example = "2022-01-01",
            required = true
        )
        String lessionDate;

        @ApiModelProperty(
            value = "PT 시간 오전",
            example = "0",
            required = true,
            dataType = "int"
        )
        Byte startAmPm;


        @ApiModelProperty(
            value = "PT 시간 오후",
            example = "0",
            required = true,
            dataType = "int"
        )
        Byte endAmPm;

        @ApiModelProperty(
            value = "PT 시간 (예, 10, 8)",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte lessonStartTime;

        @ApiModelProperty(
            value = "PT 분 ",
            example = "11",
            required = true,
            dataType = "int"
        )
        Byte lessonStartMinute;

        @ApiModelProperty(
            value = "PT 시간 (예, 10, 8)",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte lessonEndTime;

        @ApiModelProperty(
            value = "PT 분 ",
            example = "25",
            required = true,
            dataType = "int"
        )
        Byte lessonEndMinute;


        @ApiModelProperty(
            value = "유저 idx",
            example = "1",
            required = true
        )
        Integer userIdx;


        @ApiModelProperty(
            value = "PT 고객의 PT 일정 동의 여부",
            example = "1",
            required = true,
            dataType = "int"
        )
        Byte confirmed;


        @ApiModelProperty(
            value = "PT 고객의 PT 일정 확인 일자",
            example = "2022-02-02",
            required = true
        )
        String confirmedDate;
}
