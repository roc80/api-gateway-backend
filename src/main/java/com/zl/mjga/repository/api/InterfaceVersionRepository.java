package com.zl.mjga.repository.api;

import static org.jooq.impl.DSL.noCondition;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.api.InterfaceVersionQueryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.ApiInterfaceVersion;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceVersionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public void logicDelete(@NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        ctx().update(ApiInterfaceVersion.API_INTERFACE_VERSION)
                .set(ApiInterfaceVersion.API_INTERFACE_VERSION.DELETED, true)
                .where(ApiInterfaceVersion.API_INTERFACE_VERSION.ID.in(ids))
                .execute();
    }

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

        // todo@lp JSONB和OffsetDateTime类型还没有应用

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
}
