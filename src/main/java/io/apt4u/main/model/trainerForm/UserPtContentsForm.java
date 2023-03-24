package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;


@ApiModel(description = "1회 체험가 저장 폼")
@Data
public class UserPtContentsForm {



        @ApiModelProperty(
            value = "PT 운동 구분 (상체, 하체, 어깨, 가슴,,)",
           example = "1",
            required = true
        )
        Integer exerciseType;

        @ApiModelProperty(
            value = "운동명",
           example = "숨쉬기",
            required = true
        )
        String exerciseName;


        @ApiModelProperty(
            value = "운동 횟수, 세트 수, 중량, 지속시간 등 상세 내용",
           example = "쏼라 쏼라",
            required = true
        )
        String exerciseDetails;
}
