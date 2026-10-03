package com.zl.mjga.repository;

import static org.jooq.generated.api_gateway.tables.Role.ROLE;
import static org.jooq.generated.api_gateway.tables.User.USER;
import static org.jooq.generated.api_gateway.tables.UserRoleMap.USER_ROLE_MAP;
import static org.jooq.impl.DSL.*;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.urp.PermissionDto;
import com.zl.mjga.dto.urp.RoleDto;
import com.zl.mjga.dto.urp.UserQueryDto;
import com.zl.mjga.dto.urp.UserRolePermissionDto;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.generated.api_gateway.tables.daos.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepository extends UserDao {

    @Autowired
    public UserRepository(Configuration configuration) {
        super(configuration);
    }

    public Result<Record> pageFetchUserBy(
            PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
        return ctx().select(asterisk(), DSL.count().over().as("total_user"))
                .from(USER)
                .where(
                        userQueryDto.getUsername() != null
                                ? USER.USERNAME.like("%" + userQueryDto.getUsername() + "%")
                                : noCondition())
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetch();
    }

    public Result<Record> pageFetchUserAggBy(
            PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
        return selectUserAgg()
                .where(
                        userQueryDto.getUsername() != null
                                ? USER.USERNAME.like("%" + userQueryDto.getUsername() + "%")
                                : noCondition())
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetch();
    }

    public UserRolePermissionDto getUserAggDtoBy(Long userId) {
        return selectUserAgg().where(USER.ID.eq(userId)).fetchOneInto(UserRolePermissionDto.class);
    }

    /** 按接入密钥查询用户（网关 ak→sk 查询使用） */
    public org.jooq.generated.api_gateway.tables.pojos.User fetchOneByAccessKey(String accessKey) {
        return ctx().selectFrom(USER)
                .where(USER.ACCESS_KEY.eq(accessKey))
                .fetchOneInto(org.jooq.generated.api_gateway.tables.pojos.User.class);
    }

    /** 按角色 code 分页查询用户聚合（含角色与权限），用于管理员申请待审批列表 */
    public Result<Record> pageFetchUserAggByRoleCode(
            PageRequestDto pageRequestDto, String roleCode) {
        return selectUserAgg()
                .where(
                        USER.ID.in(
                                select(USER_ROLE_MAP.USER_ID)
                                        .from(USER_ROLE_MAP)
                                        .join(ROLE)
                                        .on(ROLE.ID.eq(USER_ROLE_MAP.ROLE_ID))
                                        .where(ROLE.CODE.eq(roleCode))))
                .orderBy(pageRequestDto.getSortFields())
                .limit(pageRequestDto.getSize())
                .offset(pageRequestDto.getOffset())
                .fetch();
    }

    public SelectJoinStep<Record> selectUserAgg() {
        return ctx().select(
                        USER.asterisk(),
                        DSL.count().over().as("total_user"),
                        multiset(
                                        select(
                                                        USER.role().asterisk(),
                                                        multiset(
                                                                        select(
                                                                                        USER.role()
                                                                                                .permission()
                                                                                                .asterisk())
                                                                                .from(
                                                                                        USER.role()
                                                                                                .permission()))
                                                                .convertFrom(
                                                                        r ->
                                                                                r.map(
                                                                                        (record) ->
                                                                                                record
                                                                                                        .into(
                                                                                                                PermissionDto
                                                                                                                        .class)))
                                                                .as("permissions"))
                                                .from(USER.role()))
                                .convertFrom(r -> r.map((record) -> record.into(RoleDto.class)))
                                .as("roles"))
                .from(USER);
    }

    public Result<Record> getUserFlatBy(Long userId) {
        return ctx().select(
                        USER.asterisk(),
                        USER.role().asterisk(),
                        USER.role().permission().asterisk())
                .from(USER)
                .leftJoin(USER.role())
                .leftJoin(USER.role().permission())
                .where(USER.ID.eq(userId))
                .fetch();
    }

    @Transactional
    public void deleteUserBy(String username) {
        ctx().delete(USER).where(USER.USERNAME.eq(username)).execute();
    }
}
