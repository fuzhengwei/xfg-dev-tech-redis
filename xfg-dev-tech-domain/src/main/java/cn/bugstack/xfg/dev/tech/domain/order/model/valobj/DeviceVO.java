package cn.bugstack.xfg.dev.tech.domain.order.model.valobj;

import lombok.*;

import java.util.Arrays;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 下单设备值对象
 * @create 2023-09-03 14:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceVO {

    /** 设备 */
    private String machine;
    /** 地址；北京、杭州 */
    private String location;
    /** 设备地址 */
    private String ipv4;
    /** 设备地址 */
    private byte[] ipv6;

}
