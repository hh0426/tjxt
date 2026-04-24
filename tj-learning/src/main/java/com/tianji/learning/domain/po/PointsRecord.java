package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.learning.enums.PointsRecordType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 学习积分记录，每个月底清零
 * </p>
 *
 * @author hh
 * @since 2026-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_record")
@ApiModel(value="PointsRecord对象", description="学习积分记录，每个月底清零")
public class PointsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("积分记录id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("积分类型")
    private PointsRecordType type;

    @ApiModelProperty("积分值")
    private Integer points;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


}
