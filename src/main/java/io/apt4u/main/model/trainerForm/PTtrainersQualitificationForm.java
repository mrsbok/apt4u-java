package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "자격증 폼")
@Data
public class PTtrainersQualitificationForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "트레이너 idx",
            example = "1",
            required = true
        )
        Integer tarinerIdx;

        // 소개
        @ApiModelProperty(
            value = "자격증 이름",
            example = "워드 프로세서",
            required = true
        )
        String licenseName;

        @ApiModelProperty(
            value = "발급기관",
            example = "상공회의소",
            required = true
        )
        String issuedOrganization;

        @ApiModelProperty(
            value = "자격증 발행일/취득일",
            example = "220222",
            required = true

        )
        String issuedDate;


}
