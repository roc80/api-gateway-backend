package com.zl.mjga.repository;

import static org.jooq.generated.mjga.tables.Permission.PERMISSION;
import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.noCondition;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.urp.PermissionQueryDto;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.mjga.tables.daos.PermissionDao;
import org.jooq.generated.mjga.tables.pojos.Permission;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRepository extends PermissionDao {

  @Autowired
  public PermissionRepository(Configuration configuration) {
    super(configuration);
  }

  public Result<Record> pageFetchPermissionBy(
      PageRequestDto pageRequestDto, PermissionQueryDto permissionQueryDto) {
    return ctx()
        .select(asterisk(), DSL.count().over().as("total_permission"))
        .from(PERMISSION)
        .where(
            CollectionUtils.isEmpty(permissionQueryDto.getPermissionIdList())
                ? noCondition()
                : PERMISSION.ID.in(permissionQueryDto.getPermissionIdList()))
        .and(
            permissionQueryDto.getPermissionId() == null
                ? noCondition()
                : PERMISSION.ID.eq(permissionQueryDto.getPermissionId()))
        .and(
            StringUtils.isEmpty(permissionQueryDto.getPermissionName())
                ? noCondition()
                : PERMISSION.NAME.like("%" + permissionQueryDto.getPermissionName() + "%"))
        .and(
            StringUtils.isEmpty(permissionQueryDto.getPermissionName())
                ? noCondition()
                : PERMISSION.CODE.eq(permissionQueryDto.getPermissionCode()))
        .orderBy(pageRequestDto.getSortFields())
        .limit(pageRequestDto.getSize())
        .offset(pageRequestDto.getOffset())
        .fetch();
  }

  public List<Permission> selectByPermissionIdIn(List<Long> permissionIdList) {
    return ctx()
        .selectFrom(PERMISSION)
        .where(PERMISSION.ID.in(permissionIdList))
        .fetchInto(Permission.class);
  }
}
