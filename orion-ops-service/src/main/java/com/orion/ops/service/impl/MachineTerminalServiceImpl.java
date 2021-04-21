package com.orion.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.orion.lang.wrapper.DataGrid;
import com.orion.lang.wrapper.Pager;
import com.orion.ops.consts.Const;
import com.orion.ops.consts.KeyConst;
import com.orion.ops.consts.protocol.TerminalConst;
import com.orion.ops.dao.MachineTerminalDAO;
import com.orion.ops.dao.MachineTerminalLogDAO;
import com.orion.ops.entity.domain.MachineInfoDO;
import com.orion.ops.entity.domain.MachineTerminalDO;
import com.orion.ops.entity.domain.MachineTerminalLogDO;
import com.orion.ops.entity.request.MachineTerminalLogRequest;
import com.orion.ops.entity.request.MachineTerminalRequest;
import com.orion.ops.entity.vo.MachineTerminalLogVO;
import com.orion.ops.entity.vo.TerminalAccessVO;
import com.orion.ops.service.api.MachineInfoService;
import com.orion.ops.service.api.MachineTerminalService;
import com.orion.ops.utils.Currents;
import com.orion.ops.utils.Valid;
import com.orion.ops.utils.ValueMix;
import com.orion.remote.TerminalType;
import com.orion.utils.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 终端service
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/3/31 17:20
 */
@Slf4j
@Service("machineTerminalService")
public class MachineTerminalServiceImpl implements MachineTerminalService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private MachineTerminalDAO machineTerminalDAO;

    @Resource
    private MachineTerminalLogDAO machineTerminalLogDAO;

    @Resource
    private MachineInfoService machineInfoService;

    @Override
    public MachineTerminalDO getMachineConfig(Long machineId) {
        MachineTerminalDO config = machineTerminalDAO.selectOne(new LambdaQueryWrapper<MachineTerminalDO>().eq(MachineTerminalDO::getMachineId, machineId));
        if (config != null) {
            return config;
        }
        MachineTerminalDO insert = new MachineTerminalDO();
        insert.setMachineId(machineId);
        insert.setTerminalType(TerminalType.XTERM_256_COLOR.getType());
        insert.setBackgroundColor(TerminalConst.BACKGROUND_COLOR);
        insert.setFontColor(TerminalConst.FONT_COLOR);
        insert.setFontSize(TerminalConst.FONT_SIZE);
        machineTerminalDAO.insert(insert);
        return insert;
    }

    @Override
    public TerminalAccessVO getAccessConfig(Long machineId) {
        // 获取机器信息
        MachineInfoDO machine = machineInfoService.selectById(machineId);
        Valid.notNull(machine, Const.INVALID_MACHINE);
        // 设置accessToken
        Long userId = Currents.getUserId();
        String token = ValueMix.base62ecbEnc(userId + "_" + System.currentTimeMillis(), TerminalConst.TERMINAL);
        // 获取终端配置
        MachineTerminalDO config = this.getMachineConfig(machineId);
        // 设置数据
        TerminalAccessVO access = new TerminalAccessVO();
        access.setAccessToken(token);
        access.setHost(machine.getMachineHost());
        access.setPort(machine.getSshPort());
        access.setMachineName(machine.getMachineName());
        access.setMachineId(machineId);
        access.setUsername(machine.getUsername());
        access.setTerminalType(config.getTerminalType());
        access.setBackgroundColor(config.getBackgroundColor());
        access.setFontSize(config.getFontSize());
        access.setFontColor(config.getFontColor());
        // 设置缓存
        String cacheKey = Strings.format(KeyConst.TERMINAL_ACCESS_TOKEN, token);
        redisTemplate.opsForValue().set(cacheKey, machineId + "", TerminalConst.TERMINAL_TOKEN_EXPIRE_S, TimeUnit.SECONDS);
        log.info("用户获取terminal accessToken uid: {} machineId: {} token: {}", userId, machineId, token);
        return access;
    }

    @Override
    public Integer setting(MachineTerminalRequest request) {
        MachineTerminalDO update = new MachineTerminalDO();
        update.setId(request.getId());
        update.setTerminalType(request.getTerminalType());
        update.setFontSize(request.getFontSize());
        update.setFontColor(request.getFontColor());
        update.setBackgroundColor(request.getBackgroundColor());
        return machineTerminalDAO.updateById(update);
    }

    @Override
    public Long addAccessLog(MachineTerminalLogDO entity) {
        machineTerminalLogDAO.insert(entity);
        return entity.getId();
    }

    @Override
    public Integer updateAccessLog(String token, MachineTerminalLogDO entity) {
        LambdaQueryWrapper<MachineTerminalLogDO> wrapper = new LambdaQueryWrapper<MachineTerminalLogDO>()
                .eq(MachineTerminalLogDO::getAccessToken, token);
        return machineTerminalLogDAO.update(entity, wrapper);
    }

    @Override
    public DataGrid<MachineTerminalLogVO> listAccessLog(MachineTerminalLogRequest request) {
        Pager<MachineTerminalLogVO> pager = Pager.of(request);
        LambdaQueryWrapper<MachineTerminalLogDO> wrapper = new LambdaQueryWrapper<MachineTerminalLogDO>()
                .like(Objects.nonNull(request.getAccessToken()), MachineTerminalLogDO::getAccessToken, request.getAccessToken())
                .like(Objects.nonNull(request.getMachineHost()), MachineTerminalLogDO::getMachineHost, request.getMachineHost())
                .like(Objects.nonNull(request.getUsername()), MachineTerminalLogDO::getUsername, request.getUsername())
                .eq(Objects.nonNull(request.getMachineId()), MachineTerminalLogDO::getMachineId, request.getMachineId())
                .eq(Objects.nonNull(request.getCloseCode()), MachineTerminalLogDO::getCloseCode, request.getCloseCode())
                .between(Objects.nonNull(request.getConnectedTimeStart()) && Objects.nonNull(request.getConnectedTimeEnd()),
                        MachineTerminalLogDO::getConnectedTime, request.getConnectedTimeStart(), request.getConnectedTimeEnd())
                .between(Objects.nonNull(request.getDisconnectedTimeStart()) && Objects.nonNull(request.getDisconnectedTimeEnd()),
                        MachineTerminalLogDO::getDisconnectedTime, request.getDisconnectedTimeStart(), request.getDisconnectedTimeEnd())
                .orderByDesc(MachineTerminalLogDO::getCreateTime);
        Integer count = machineTerminalLogDAO.selectCount(wrapper);
        pager.setTotal(count);
        boolean next = pager.hasMoreData();
        if (next) {
            wrapper.last(pager.getSql());
            List<MachineTerminalLogVO> rows = machineTerminalLogDAO.selectList(wrapper).stream()
                    .map(p -> {
                        MachineTerminalLogVO vo = new MachineTerminalLogVO();
                        vo.setId(p.getId());
                        vo.setUserId(p.getUserId());
                        vo.setUsername(p.getUsername());
                        vo.setMachineId(p.getMachineId());
                        vo.setMachineHost(p.getMachineHost());
                        vo.setAccessToken(p.getAccessToken());
                        vo.setConnectedTime(p.getConnectedTime());
                        vo.setDisconnectedTime(p.getDisconnectedTime());
                        vo.setCloseCode(p.getCloseCode());
                        vo.setCreateTime(p.getCreateTime());
                        return vo;
                    }).collect(Collectors.toList());
            pager.setRows(rows);
        }
        return DataGrid.of(pager);
    }

    @Override
    public Integer checkLogExist(Long id) {
        return Optional.ofNullable(machineTerminalLogDAO.selectById(id))
                .map(MachineTerminalLogDO::getOperateLogFile)
                .filter(Strings::isNotBlank)
                .map(File::new)
                .map(f -> f.exists() && f.isFile())
                .map(s -> s ? 1 : 2)
                .orElse(2);
    }

    @Override
    public String getLogFilePath(Long id) {
        return Optional.ofNullable(machineTerminalLogDAO.selectById(id))
                .map(MachineTerminalLogDO::getOperateLogFile)
                .orElse(null);
    }

}
