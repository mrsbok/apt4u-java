package io.apt4u.main.model.adminForm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "총괄 가입 정보 폼 입니다.")
@Data
public class AdminForm {
    Integer idx = null;

    // 총괄 ID
    @ApiModelProperty(
            value = "총괄 ID",
            example = "admin",
            required = true
    )
    String id;

    // 총괄 패스워드
    @ApiModelProperty(
            value = "총괄 패스워드",
            example = "admin",
            required = true
    )
    String password;

    // 총괄 담당자명
    @ApiModelProperty(
            value = "총괄 담당자명",
            example = "홍길동"
    )
    String personInChargeName;

    // 총괄 담당자명
    @ApiModelProperty(
            value = "총괄 담당자 연락처",
            example = "0511234567"
    )
    String contactNumber;
}
