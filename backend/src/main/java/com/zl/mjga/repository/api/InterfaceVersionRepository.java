package com.zl.mjga.repository.api;

import static org.jooq.impl.DSL.noCondition;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import com.zl.mjga.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.ApiInterfaceVersion;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceVersionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author roc
 * @since 2025/12/25 20:08
 */
@Repository
public class InterfaceVersionRepository extends ApiInterfaceVersionDao {
    @Autowired
    public InterfaceVersionRepository(Configuration configuration) {
        super(configuration);
    }

    @Transactional(rollbackFor = BusinessException.class)
    public void logicDelete(@NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        ctx().update(ApiInterfaceVersion.API_INTERFACE_VERSION)
                .set(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED, true)
                .where(ApiInterfaceVersion.API_INTERFACE_VERSION.ID.in(ids))
                .execute();
    }

    @Transactional(rollbackFor = BusinessException.class)
    public void logicDelete(@Positive(message = "接口版本ID必须为正整数") Long id) {
        ctx().update(ApiInterfaceVersion.API_INTERFACE_VERSION)
                .set(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED, true)
                .where(ApiInterfaceVersion.API_INTERFACE_VERSION.ID.eq(id))
                .execute();
    }

    public List<org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion>
    fetchByPageRequestDto(@Valid PageRequestDto<InterfaceVersionQueryDto> pageRequestDto) {
        Condition condition = buildCondition(pageRequestDto.getRequest());

        return ctx().selectFrom(ApiInterfaceVersion.API_INTERFACE_VERSION)
                .where(condition)
                .and(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED.eq(false))
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetchInto(org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion.class);
    }

    private Condition buildCondition(InterfaceVersionQueryDto request) {
        if (request == null) {
            return noCondition();
        }
        List<Condition> conditions = new ArrayList<>();

        if (request.apiId() != null) {
            conditions.add(ApiInterfaceVersion.API_INTERFACE_VERSION.API_ID.eq(request.apiId()));
        }
        if (StringUtils.isNotBlank(request.version())) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.VERSION.like(
                            "%" + request.version() + "%"));
        }
        if (request.current() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.IS_CURRENT.eq(request.current()));
        }
        if (StringUtils.isNotBlank(request.httpMethod())) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.HTTP_METHOD.eq(request.httpMethod()));
        }
        if (StringUtils.isNotBlank(request.path())) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.PATH.like(
                            "%" + request.path() + "%"));
        }
        if (StringUtils.isNotBlank(request.authType())) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.AUTH_TYPE.like(
                            "%" + request.authType() + "%"));
        }
        if (request.allowInvoke() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.ALLOW_INVOKE.eq(
                            request.allowInvoke()));
        }
        if (request.requestHeaders() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.REQUEST_HEADERS.contains(
                            JSONB.jsonb(request.requestHeaders())));
        }
        if (request.createTimeStart() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.CREATE_TIME.greaterOrEqual(
                            request.createTimeStart()));
        }
        if (request.createTimeEnd() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.CREATE_TIME.lessOrEqual(
                            request.createTimeEnd()));
        }
        if (request.updateTimeStart() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.UPDATE_TIME.greaterOrEqual(
                            request.updateTimeStart()));
        }
        if (request.updateTimeEnd() != null) {
            conditions.add(
                    ApiInterfaceVersion.API_INTERFACE_VERSION.UPDATE_TIME.lessOrEqual(
                            request.updateTimeEnd()));
        }

        return conditions.isEmpty()
                ? noCondition()
                : conditions.stream().reduce(Condition::and).orElse(noCondition());
    }

    public long countByQueryDto(InterfaceVersionQueryDto request) {
        Condition condition = buildCondition(request);

        Long count =
                ctx().selectCount()
                        .from(ApiInterfaceVersion.API_INTERFACE_VERSION)
                        .where(condition)
                        .and(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED.eq(false))
                        .fetchOneInto(Long.class);
        return count == null ? 0 : count;
    }

    public org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion findByApiIdAndVersion(Long apiId, String apiVersion) {
        return ctx().selectFrom(ApiInterfaceVersion.API_INTERFACE_VERSION)
                .where(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED.eq(false))
                .and(ApiInterfaceVersion.API_INTERFACE_VERSION.API_ID.eq(apiId))
                .and(ApiInterfaceVersion.API_INTERFACE_VERSION.VERSION.eq(apiVersion))
                .fetchOneInto(org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion.class);
    }
}
