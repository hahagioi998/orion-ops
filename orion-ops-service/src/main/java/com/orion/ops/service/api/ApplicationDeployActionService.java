package com.orion.ops.service.api;

import com.orion.ops.entity.vo.ApplicationDeployActionVO;

import java.util.List;

/**
 * app部署步骤接口
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/7/8 18:30
 */
public interface ApplicationDeployActionService {

    /**
     * 删除部署步骤
     *
     * @param appId     appId
     * @param profileId profileId
     * @return effect
     */
    Integer deleteAppActionByAppProfileId(Long appId, Long profileId);

    /**
     * 获取部署步骤
     *
     * @param appId     appId
     * @param profileId profileId
     * @return actions
     */
    List<ApplicationDeployActionVO> getDeployActions(Long appId, Long profileId);

}
