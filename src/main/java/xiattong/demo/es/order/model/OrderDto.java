package xiattong.demo.es.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author ：xiattong
 * @description：订单对象
 * @version: $
 * @date ：Created in 2022/10/16 22:21
 * @modified By：
 */
@Getter
@Setter
@ToString
public class OrderDto implements Serializable {
    /** id*/
    private Long id;
    /** 订单号*/
    private String orderNo;
    /** 商家id*/
    private Long sellerId;
    /** 用户id*/
    private Long userId;
    /** 订单金额*/
    private BigDecimal amount;
    /** 订单状态*/
    private Integer status;
    /** 下单时间*/
    private String orderAt;
    /** 版本号*/
    private Long version;
    /** 删除标识*/
    private Integer isDeleted;
}
