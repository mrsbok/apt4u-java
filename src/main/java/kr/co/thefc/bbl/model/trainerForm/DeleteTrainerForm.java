package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "트레이너 회원 운동 기록 폼")
@Data
public class DeleteTrainerForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer userIdx;

        @ApiModelProperty(
            value = "트레이너 ID",
            example = "asd123@gmail.com",
            required = true
        )
        String id;

        @ApiModelProperty(
            value = "마지막 접속일",
           example = "2022-01-01 00:00:00",
            required = true
        )
        String lastAccess;

        @ApiModelProperty(
            value = "탈퇴 사유",
           example = "1",
            required = true,
            dataType = "int"
        )
        Byte deletedReason;


        @ApiModelProperty(
            value = "상세 탈퇴 사유",
           example = "똥이 너무 마려워서...",
            required = true
        )
        String reasonDetail;


}
