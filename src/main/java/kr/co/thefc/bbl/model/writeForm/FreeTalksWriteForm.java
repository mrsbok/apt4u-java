package kr.co.thefc.bbl.model.writeForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "자유톡 작성 폼")
@Data
public class FreeTalksWriteForm {
    Integer freeTalkIdx = null;

    @ApiModelProperty(value = "자유톡 제목", required = true, example = "테스트 제목입니다.")
    String title;

    @ApiModelProperty(value = "자유톡 내용", required = true, example = "테스트 내용입니다.")
    String content;

    @ApiModelProperty(value = "자유톡 작성자 식별 번호", required = true, example = "1")
    Integer writerIdx;

    String writerName;
}
