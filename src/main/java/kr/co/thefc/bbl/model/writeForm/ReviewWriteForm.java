package kr.co.thefc.bbl.model.writeForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "후기 작성 폼")
@Data
public class ReviewWriteForm {

    Integer noteCategory;

    @ApiModelProperty(value = "후기 대상 식별번호", required = true, example = "1")
    Integer targetIdx;

    @ApiModelProperty(value = "사용자 평점", required = true, example = "5")
    Integer userSatisfaction;

    @ApiModelProperty(value = "이용 시작일", required = true, example = "2022-06-20")
    String useStartDate;

    @ApiModelProperty(value = "이용 종료일", required = true, example = "2022-06-27")
    String useEndDate;

    @ApiModelProperty(value = "후기 내용", required = true, example = "후기 내용 작성")
    String content;

    @ApiModelProperty(value = "해시태그", required = false, example = "#해시태그")
    String hashtag;

    @ApiModelProperty(value = "사용자 식별 번호", required = true, example = "1")
    Integer userIdx;
}
