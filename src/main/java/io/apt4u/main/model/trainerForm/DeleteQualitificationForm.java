package io.apt4u.main.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "자격증 폼")
@Data
public class DeleteQualitificationForm {

        Integer idx = null;


        @ApiModelProperty(
            value = "이미지 시퀀스 번호",
            example = "1",
            required = true
        )
        Integer iseq;

        // 소개
        @ApiModelProperty(
            value = "경력 테이블 시퀀스 번호",
            example = "1",
            required = true
        )
        Integer workExprienceIseq;

        @ApiModelProperty(
            value = "수상 내역 테이블 시퀀스 번호",
            example = "1",
            required = true
        )
        Integer awardWinningIseq;

        @ApiModelProperty(
            value = "자격증 테이블 시퀀스 번호",
            example = "1",
            required = true

        )
        Integer qualitificationIseq;

        @ApiModelProperty(
            value = "이미지 타입",
            example = "자격증",
            required = true

        )
        String imageType;


}
