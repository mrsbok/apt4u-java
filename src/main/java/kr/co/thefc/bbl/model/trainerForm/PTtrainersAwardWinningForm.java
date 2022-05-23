package kr.co.thefc.bbl.model.trainerForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "수상내역 폼")
@Data
public class PTtrainersAwardWinningForm {

        Integer idx = null;

        // 소개
        @ApiModelProperty(
            value = "년도 (대회년도)",
            example = "2022",
            required = true
        )
        String year;

        @ApiModelProperty(
            value = "대회명",
            example = "빨리먹기",
            required = true
        )
        String competition;

        @ApiModelProperty(
            value = "출전 부문",
            example = "밥2공기 10초안에 먹기"
        )
        String participation;

        @ApiModelProperty(
            value = "수상내역",
            example = "1등",
            required = true
        )
        String awardWinning;

}
