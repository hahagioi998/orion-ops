package com.orion.ops.consts.machine;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 机器环境变量key
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/3/29 15:42
 */
@Getter
public enum MachineEnvAttr {

    /**
     * 存放秘钥文件目录
     */
    KEY_PATH("秘钥目录"),

    /**
     * 存放图片目录
     */
    PIC_PATH("图片目录"),

    /**
     * 临时交换目录
     */
    SWAP_PATH("临时交换目录"),

    /**
     * 日志目录
     */
    LOG_PATH("日志目录"),

    /**
     * 临时文件目录
     */
    TEMP_PATH("临时目录"),

    /**
     * 应用版本仓库目录
     */
    VCS_PATH("应用版本仓库目录"),

    /**
     * 构建产物目录
     */
    DIST_PATH("构建产物目录"),

    /**
     * sftp 文件名称编码格式 默认UTF-8
     */
    SFTP_CHARSET("SFTP 文件名称编码格式"),

    /**
     * 文件追踪模式 tracker或tail 默认tracker
     */
    TAIL_MODE("文件追踪模式 (tracker/tail)"),

    /**
     * 文件追踪偏移量
     */
    TAIL_OFFSET("文件追踪偏移量(行)"),

    /**
     * 文件追踪编码格式
     */
    TAIL_CHARSET("文件追踪编码格式"),

    ;

    /**
     * key
     */
    private final String key;

    /**
     * 描述
     */
    private final String description;

    @Setter
    private String value;

    MachineEnvAttr(String description) {
        this.description = description;
        this.key = this.name().toLowerCase();
    }

    public static List<String> getKeys() {
        return Arrays.stream(values())
                .map(MachineEnvAttr::getKey)
                .collect(Collectors.toList());
    }

    public static MachineEnvAttr of(String key) {
        if (key == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(a -> a.key.equals(key))
                .findFirst()
                .orElse(null);
    }

}
