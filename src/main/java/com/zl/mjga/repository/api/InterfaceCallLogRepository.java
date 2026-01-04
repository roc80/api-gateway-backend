package com.zl.mjga.repository.api;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.api.InterfaceCallLogQueryDto;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.jooq.generated.api_gateway.tables.ApiInterfaceCallLog;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceCallLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.noCondition;

/**
 * @author roc
 * @since 2025/12/25 20:09
 */
@Repository
public class InterfaceCallLogRepository extends ApiInterfaceCallLogDao {
    @Autowired
    public InterfaceCallLogRepository(Configuration configuration) {
        super(configuration);
    }

    public long countByQueryDto(InterfaceCallLogQueryDto queryDto) {
        return ctx().fetchCount(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG, buildCondition(queryDto));
    }

    public List<org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog> fetchByPageRequestDto(
            @Valid PageRequestDto<InterfaceCallLogQueryDto> pageRequestDto) {
        Condition condition = buildCondition(pageRequestDto.getRequest());

        return ctx().selectFrom(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG)
                .where(condition)
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetchInto(org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog.class);
    }

    private Condition buildCondition(InterfaceCallLogQueryDto queryDto) {
        if (queryDto == null) {
            return noCondition();
        }

        List<Condition> conditions = new ArrayList<>();

        if (queryDto.apiId() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.API_ID.eq(queryDto.apiId()));
        }
        if (queryDto.versionId() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.VERSION_ID.eq(queryDto.versionId()));
        }
        if (StringUtils.isNotBlank(queryDto.caller())) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CALLER.eq(queryDto.caller()));
        }
        if (queryDto.statusCode() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.STATUS_CODE.eq(queryDto.statusCode()));
        }
        if (queryDto.success() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.SUCCESS.eq(queryDto.success()));
        }
        if (queryDto.durationMs() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.DURATION_MS.eq(queryDto.durationMs()));
        }
        if (queryDto.createTime() != null) {
            conditions.add(ApiInterfaceCallLog.API_INTERFACE_CALL_LOG.CREATE_TIME.eq(queryDto.createTime()));
        }

        return conditions.isEmpty() ? noCondition() : DSL.and(conditions);
    }
}
