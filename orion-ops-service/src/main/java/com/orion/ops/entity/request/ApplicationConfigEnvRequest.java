package com.orion.ops.entity.request;

import lombok.Data;

/**
 * app配置环境请求
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/7/5 18:50
 */
@Data
public class ApplicationConfigEnvRequest {

    /**
     * 构建产物路径
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#BUNDLE_PATH
     */
    private String bundlePath;

    /**
     * 产物传输绝对路径
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#TRANSFER_PATH
     */
    private String transferPath;

    /**
     * 产物传输方式 (sftp/scp)
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#TRANSFER_MODE
     */
    private String transferMode;

    /**
     * 产物传输文件类型 (normal/zip)
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#TRANSFER_FILE_TYPE
     */
    private String transferFileType;

    /**
     * 发布序列 10串行 20并行
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#RELEASE_SERIAL
     * @see com.orion.ops.consts.SerialType
     */
    private Integer releaseSerial;

    /**
     * 异常处理 10跳过所有 20跳过错误
     *
     * @see com.orion.ops.consts.app.ApplicationEnvAttr#EXCEPTION_HANDLER
     * @see com.orion.ops.consts.ExceptionHandlerType
     * @see com.orion.ops.consts.SerialType#SERIAL
     */
    private Integer exceptionHandler;

}
