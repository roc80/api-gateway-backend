package com.zl.mjga.repository.api;

import static org.jooq.impl.DSL.noCondition;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.api.InterfaceQueryDto;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.ApiInterface;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author roc
 * @since 2025/12/25 19:59
 */
@Repository
public class InterfaceRepository extends ApiInterfaceDao {
    @Autowired
    public InterfaceRepository(Configuration configuration) {
        super(configuration);
    }

    public List<org.jooq.generated.api_gateway.tables.pojos.ApiInterface> fetchByPageRequestDto(
            @Valid PageRequestDto<InterfaceQueryDto> pageRequestDto) {
        Condition condition = buildCondition(pageRequestDto.getRequest());

        return ctx().selectFrom(ApiInterface.API_INTERFACE)
                .where(condition)
                .and(ApiInterface.API_INTERFACE.DELETED.eq(false))
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetchInto(org.jooq.generated.api_gateway.tables.pojos.ApiInterface.class);
    }

    public long countByQueryDto(InterfaceQueryDto queryDto) {
        Condition condition = buildCondition(queryDto);

        Long count =
                ctx().selectCount()
                        .from(ApiInterface.API_INTERFACE)
                        .where(condition)
                        .and(ApiInterface.API_INTERFACE.DELETED.eq(false))
                        .fetchOne(0, Long.class);

        return count == null ? 0 : count;
    }

    /** 根据查询条件构建 JOOQ Condition */
    private Condition buildCondition(InterfaceQueryDto queryDto) {
        if (queryDto == null) {
            return noCondition();
        }

        List<Condition> conditions = new ArrayList<>();

        if (StringUtils.isNotBlank(queryDto.name())) {
            conditions.add(ApiInterface.API_INTERFACE.NAME.like("%" + queryDto.name() + "%"));
        }
        if (StringUtils.isNotBlank(queryDto.code())) {
            conditions.add(ApiInterface.API_INTERFACE.CODE.like("%" + queryDto.code() + "%"));
        }
        if (StringUtils.isNotBlank(queryDto.description())) {
            conditions.add(
                    ApiInterface.API_INTERFACE.DESCRIPTION.like(
                            "%" + queryDto.description() + "%"));
        }
        if (queryDto.enabled() != null) {
            conditions.add(ApiInterface.API_INTERFACE.ENABLED.eq(queryDto.enabled()));
        }
        if (StringUtils.isNotBlank(queryDto.category())) {
            conditions.add(
                    ApiInterface.API_INTERFACE.CATEGORY.like("%" + queryDto.category() + "%"));
        }
        if (StringUtils.isNotBlank(queryDto.owner())) {
            conditions.add(ApiInterface.API_INTERFACE.OWNER.like("%" + queryDto.owner() + "%"));
        }
        if (queryDto.createTimeStart() != null) {
            conditions.add(
                    ApiInterface.API_INTERFACE.CREATE_TIME.greaterOrEqual(
                            queryDto.createTimeStart()));
        }
        if (queryDto.createTimeEnd() != null) {
            conditions.add(
                    ApiInterface.API_INTERFACE.CREATE_TIME.lessOrEqual(queryDto.createTimeEnd()));
        }
        if (queryDto.updateTimeStart() != null) {
            conditions.add(
                    ApiInterface.API_INTERFACE.UPDATE_TIME.greaterOrEqual(
                            queryDto.updateTimeStart()));
        }
        if (queryDto.updateTimeEnd() != null) {
            conditions.add(
                    ApiInterface.API_INTERFACE.UPDATE_TIME.lessOrEqual(queryDto.updateTimeEnd()));
        }

        return conditions.isEmpty()
                ? noCondition()
                : conditions.stream().reduce(Condition::and).orElse(noCondition());
    }

    public void logicDelete(Long id) {
        ctx().update(ApiInterface.API_INTERFACE)
                .set(ApiInterface.API_INTERFACE.DELETED, true)
                .where(ApiInterface.API_INTERFACE.ID.eq(id))
                .execute();
    }

    public void logicDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ctx().update(ApiInterface.API_INTERFACE)
                .set(ApiInterface.API_INTERFACE.DELETED, true)
                .where(ApiInterface.API_INTERFACE.ID.in(ids))
                .execute();
    }
}
