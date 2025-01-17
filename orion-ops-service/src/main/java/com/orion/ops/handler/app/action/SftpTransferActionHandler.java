package com.orion.ops.handler.app.action;

import com.orion.ops.consts.Const;
import com.orion.ops.consts.StainCode;
import com.orion.ops.consts.system.SystemEnvAttr;
import com.orion.ops.service.api.MachineEnvService;
import com.orion.ops.utils.Utils;
import com.orion.remote.channel.sftp.SftpExecutor;
import com.orion.spring.SpringHolder;
import com.orion.utils.Exceptions;
import com.orion.utils.collect.Maps;
import com.orion.utils.io.Files1;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 执行操作-传输产物 sftp方式
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @see com.orion.ops.consts.app.ActionType#RELEASE_TRANSFER
 * @see com.orion.ops.consts.app.TransferMode#SFTP
 * @since 2022/4/26 23:57
 */
public class SftpTransferActionHandler extends AbstractTransferActionHandler<SftpExecutor> {

    protected static MachineEnvService machineEnvService = SpringHolder.getBean(MachineEnvService.class);

    public SftpTransferActionHandler(Long actionId, MachineActionStore store) {
        super(actionId, store);
    }

    @Override
    protected void handler() throws Exception {
        // 检查文件
        String bundlePath = Files1.getPath(SystemEnvAttr.DIST_PATH.getValue(), store.getBundlePath());
        File bundleFile = new File(bundlePath);
        if (!bundleFile.exists()) {
            throw Exceptions.log("*** 产物文件不存在 " + bundlePath);
        }
        // 打开executor
        String charset = machineEnvService.getSftpCharset(store.getMachineId());
        this.executor = store.getSessionStore().getSftpExecutor(charset);
        executor.connect();
        // 删除远程文件
        String transferPath = store.getTransferPath();
        executor.rm(transferPath);
        String space = "      ";
        String bundleAbsolutePath = bundleFile.getAbsolutePath();
        // 拼接头文件
        StringBuilder headerLog = new StringBuilder(Const.LF)
                .append(space)
                .append(Utils.getStainKeyWords("source:    ", StainCode.GLOSS_GREEN))
                .append(Utils.getStainKeyWords(bundleAbsolutePath, StainCode.GLOSS_BLUE))
                .append(Const.LF)
                .append(space)
                .append(Utils.getStainKeyWords("target:    ", StainCode.GLOSS_GREEN))
                .append(Utils.getStainKeyWords(transferPath, StainCode.GLOSS_BLUE))
                .append(Const.LF_2);
        headerLog.append(StainCode.prefix(StainCode.GLOSS_GREEN))
                .append(space)
                .append("类型")
                .append(space)
                .append(" target")
                .append(StainCode.SUFFIX)
                .append(Const.LF);
        this.appendLog(headerLog.toString());
        // 转化文件
        Map<File, String> transferFiles = this.convertFile(bundleFile, transferPath);
        for (Map.Entry<File, String> entity : transferFiles.entrySet()) {
            File localFile = entity.getKey();
            String remoteFile = entity.getValue();
            // 文件夹则创建
            if (localFile.isDirectory()) {
                StringBuilder createDirLog = new StringBuilder(space)
                        .append(Utils.getStainKeyWords("mkdir", StainCode.GLOSS_GREEN))
                        .append(space)
                        .append(Utils.getStainKeyWords(remoteFile, StainCode.GLOSS_BLUE))
                        .append(Const.LF);
                this.appendLog(createDirLog.toString());
                executor.mkdirs(remoteFile);
                continue;
            }
            // 文件则传输
            StringBuilder transferLog = new StringBuilder(space)
                    .append(Utils.getStainKeyWords("touch", StainCode.GLOSS_GREEN))
                    .append(space)
                    .append(Utils.getStainKeyWords(remoteFile, StainCode.GLOSS_BLUE))
                    .append(StainCode.prefix(StainCode.GLOSS_BLUE))
                    .append(" (")
                    .append(Files1.getSize(localFile.length()))
                    .append(")")
                    .append(StainCode.SUFFIX)
                    .append(Const.LF);
            this.appendLog(transferLog.toString());
            executor.uploadFile(remoteFile, Files1.openInputStreamFast(localFile), true);
        }
        this.appendLog(Const.LF);
    }

    /**
     * 转化文件
     *
     * @param bundleFile   打包文件
     * @param transferPath 传输目录
     * @return transferFiles
     */
    private Map<File, String> convertFile(File bundleFile, String transferPath) {
        Map<File, String> map = Maps.newLinkedMap();
        if (bundleFile.isFile()) {
            map.put(bundleFile, transferPath);
            return map;
        }
        // 如果是文件夹则需要截取
        String bundleFileAbsolutePath = bundleFile.getAbsolutePath();
        List<File> transferFiles = Files1.listFiles(bundleFile, true, true);
        for (File transferFile : transferFiles) {
            String remoteFile = Files1.getPath(transferPath, transferFile.getAbsolutePath().substring(bundleFileAbsolutePath.length() + 1));
            map.put(transferFile, remoteFile);
        }
        return map;
    }

}
