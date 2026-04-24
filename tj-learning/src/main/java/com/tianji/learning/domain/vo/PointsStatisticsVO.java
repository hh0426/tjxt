package com.tianji.learning.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 积分统计VO
 * </p>
 *
 * @author hh
 * @since 2026-04-24
 */
@Data
@ApiModel(description = "积分统计信息")
public class PointsStatisticsVO {

    @ApiModelProperty("积分类型描述")
    private String type;

    @ApiModelProperty("积分上限")
    private Integer maxPoints;

    @ApiModelProperty("已获得的积分")
    private Integer points;
}
