package com.zl.mjga.service;

import com.roc.api.dto.InvokeTargetInfo;
import com.roc.api.service.InterfaceQueryService;
import com.zl.mjga.repository.api.InterfaceVersionRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;

/**
 * 网关侧接口目标查询 RPC 实现：按方法+路径判断接口是否存在且可调用，供网关转发前校验
 *
 * @author roc
 * @since 2026/10/3
 */
@DubboService
public class InterfaceQueryServiceImpl implements InterfaceQueryService {

    private final InterfaceVersionRepository interfaceVersionRepository;

    public InterfaceQueryServiceImpl(InterfaceVersionRepository interfaceVersionRepository) {
        this.interfaceVersionRepository = interfaceVersionRepository;
    }

    @Override
    public InvokeTargetInfo getInvokeTarget(String httpMethod, String path) {
        if (StringUtils.isBlank(httpMethod) || StringUtils.isBlank(path)) {
            return null;
        }
        ApiInterfaceVersion version =
                interfaceVersionRepository.fetchInvokeTarget(httpMethod, path);
        if (version == null) {
            return null;
        }
        return new InvokeTargetInfo(version.getApiId(), version.getId());
    }
}
