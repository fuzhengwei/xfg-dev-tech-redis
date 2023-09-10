package cn.bugstack.xfg.dev.tech.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description sku 实体对象
 * @create 2023-09-03 14:53
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SKUEntity {

    /** 商品编号 */
    private String sku;
    /** 商品名称 */
    private String skuName;
    /** 商品数量 */
    private int quantity;
    /** 商品价格 */
    private BigDecimal unitPrice;
    /** 折扣金额 */
    private BigDecimal discountAmount;
    /** 费率金额 */
    private BigDecimal tax;
    /** 支付金额 */
    private BigDecimal totalAmount;

}
