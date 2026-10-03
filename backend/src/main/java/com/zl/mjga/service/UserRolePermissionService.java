package com.zl.mjga.service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.urp.*;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.model.urp.ERole;
import com.zl.mjga.repository.*;
import com.zl.mjga.utils.BeanUtils;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.api_gateway.tables.pojos.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRolePermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapRepository userRoleMapRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapRepository rolePermissionMapRepository;
    private final PasswordEncoder passwordEncoder;

    public void upsertUser(@Valid UserUpsertDto userUpsertDto) {
        User user;
        if (userUpsertDto.getId() != null) {
            user = userRepository.fetchOneById(userUpsertDto.getId());
            if (user == null) {
                throw new BusinessException("User not found with id: " + userUpsertDto.getId());
            }
        } else {
            user = new User();
        }
        BeanUtils.copyPropertiesIgnoreBlank(userUpsertDto, user);
        if (StringUtils.isNotBlank(userUpsertDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userUpsertDto.getPassword()));
        }
        userRepository.merge(user);
    }

    public void upsertRole(RoleUpsertDto roleUpsertDto) {
        Role role;
        if (roleUpsertDto.getId() != null) {
            role = roleRepository.fetchOneById(roleUpsertDto.getId());
            if (role == null) {
                throw new BusinessException("Role not found with id: " + roleUpsertDto.getId());
            }
        } else {
            role = new Role();
        }
        BeanUtils.copyPropertiesIgnoreBlank(roleUpsertDto, role);
        roleRepository.merge(role);
    }

    public void upsertPermission(PermissionUpsertDto permissionUpsertDto) {
        Permission permission;
        if (permissionUpsertDto.getId() != null) {
            permission = permissionRepository.fetchOneById(permissionUpsertDto.getId());
            if (permission == null) {
                throw new BusinessException(
                        "Permission not found with id: " + permissionUpsertDto.getId());
            }
        } else {
            permission = new Permission();
        }
        BeanUtils.copyPropertiesIgnoreBlank(permissionUpsertDto, permission);
        permissionRepository.merge(permission);
    }

    public PageResponseDto<List<UserRolePermissionDto>> pageQueryUserAgg(
            PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
        Result<Record> userRecords =
                userRepository.pageFetchUserAggBy(pageRequestDto, userQueryDto);
        if (userRecords.isEmpty()) {
            return PageResponseDto.empty();
        }
        List<UserRolePermissionDto> nestedUserAgg = userRecords.into(UserRolePermissionDto.class);
        return new PageResponseDto<>(
                userRecords.get(0).getValue("total_user", Integer.class), nestedUserAgg);
    }

    public @Nullable UserRolePermissionDto queryUniqueUserWithRolePermission(Long userId) {
        return userRepository.getUserAggDtoBy(userId);
    }

    public PageResponseDto<List<RoleDto>> pageQueryRoleAgg(
            PageRequestDto pageRequestDto, RoleQueryDto roleQueryDto) {
        Result<Record> roleRecords =
                roleRepository.pageFetchRoleAggBy(pageRequestDto, roleQueryDto);
        if (roleRecords.isEmpty()) {
            return PageResponseDto.empty();
        }
        List<RoleDto> roleAgg = roleRecords.into(RoleDto.class);
        return new PageResponseDto<>(
                roleRecords.get(0).getValue("total_role", Integer.class), roleAgg);
    }

    public PageResponseDto<List<PermissionDto>> pageQueryPermission(
            PageRequestDto pageRequestDto, PermissionQueryDto permissionQueryDto) {
        Result<Record> permissionRecords =
                permissionRepository.pageFetchPermissionBy(pageRequestDto, permissionQueryDto);
        if (permissionRecords.isEmpty()) {
            return PageResponseDto.empty();
        }
        List<PermissionDto> permissions = permissionRecords.into(PermissionDto.class);
        return new PageResponseDto<>(
                permissionRecords.get(0).getValue("total_permission", Integer.class), permissions);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bindPermissionToRole(Long roleId, List<Long> permissionIdList) {
        rolePermissionMapRepository.deleteByRoleId(roleId);
        if (CollectionUtils.isEmpty(permissionIdList)) {
            return;
        }
        List<Permission> permissions =
                permissionRepository.selectByPermissionIdIn(permissionIdList);
        if (CollectionUtils.isEmpty(permissions)) {
            throw new BusinessException("bind permission not exist");
        }
        List<RolePermissionMap> permissionMapList =
                permissions.stream()
                        .map(
                                (permission -> {
                                    RolePermissionMap rolePermissionMap = new RolePermissionMap();
                                    rolePermissionMap.setRoleId(roleId);
                                    rolePermissionMap.setPermissionId(permission.getId());
                                    return rolePermissionMap;
                                }))
                        .collect(Collectors.toList());
        rolePermissionMapRepository.insert(permissionMapList);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bindRoleToUser(Long userId, List<Long> roleIdList) {
        userRoleMapRepository.deleteByUserId(userId);
        if (CollectionUtils.isEmpty(roleIdList)) {
            return;
        }
        List<Role> roles = roleRepository.selectByRoleIdIn(roleIdList);
        if (CollectionUtils.isEmpty(roles)) {
            throw new BusinessException("bind role not exist");
        }
        List<UserRoleMap> userRoleMapList =
                roles.stream()
                        .map(
                                (role -> {
                                    UserRoleMap userRoleMap = new UserRoleMap();
                                    userRoleMap.setUserId(userId);
                                    userRoleMap.setRoleId(role.getId());
                                    return userRoleMap;
                                }))
                        .collect(Collectors.toList());
        userRoleMapRepository.insert(userRoleMapList);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bindRoleModuleToUser(Long userId, List<ERole> eRoleList) {
        bindRoleToUser(
                userId,
                roleRepository
                        .selectByRoleCodeIn(
                                eRoleList.stream().map(Enum::name).collect(Collectors.toList()))
                        .stream()
                        .map(Role::getId)
                        .toList());
    }

    public boolean isRoleDuplicate(String roleCode, String name) {
        return roleRepository.fetchOneByCode(roleCode) != null
                || roleRepository.fetchOneByName(name) != null;
    }

    public boolean isUsernameDuplicate(String username) {
        return userRepository.fetchOneByUsername(username) != null;
    }

    public boolean isPermissionDuplicate(String code, String name) {
        return permissionRepository.fetchOneByCode(code) != null
                || permissionRepository.fetchOneByName(name) != null;
    }

    /** 申请成为管理员：绑定 PENDING_ADMIN 标记角色，等待管理员审批 */
    @Transactional(rollbackFor = Throwable.class)
    public void applyForAdmin(Long userId) {
        List<Role> currentRoles = getCurrentRolesOrThrow(userId);
        ensureRoleAbsent(currentRoles, ERole.ADMIN, "已是管理员，无需申请");
        ensureRoleAbsent(currentRoles, ERole.PENDING_ADMIN, "已提交管理员申请，请等待审批");
        List<Role> newRoles = new ArrayList<>(currentRoles);
        newRoles.add(roleByCodeOrThrow(ERole.PENDING_ADMIN));
        rebindRoles(userId, newRoles);
    }

    /** 审批通过管理员申请：PENDING_ADMIN 置换为 ADMIN */
    @Transactional(rollbackFor = Throwable.class)
    public void approveAdminApplication(Long userId) {
        List<Role> currentRoles = getCurrentRolesOrThrow(userId);
        Role pendingRole = ensureRolePresent(currentRoles, ERole.PENDING_ADMIN, "该用户没有待审批的管理员申请");
        List<Role> newRoles =
                currentRoles.stream()
                        .filter(role -> !role.getId().equals(pendingRole.getId()))
                        .collect(Collectors.toList());
        newRoles.add(roleByCodeOrThrow(ERole.ADMIN));
        rebindRoles(userId, newRoles);
    }

    /** 驳回管理员申请：移除 PENDING_ADMIN */
    @Transactional(rollbackFor = Throwable.class)
    public void rejectAdminApplication(Long userId) {
        List<Role> currentRoles = getCurrentRolesOrThrow(userId);
        Role pendingRole = ensureRolePresent(currentRoles, ERole.PENDING_ADMIN, "该用户没有待审批的管理员申请");
        List<Role> newRoles =
                currentRoles.stream()
                        .filter(role -> !role.getId().equals(pendingRole.getId()))
                        .collect(Collectors.toList());
        rebindRoles(userId, newRoles);
    }

    /** 待审批管理员申请列表（PENDING_ADMIN 角色用户） */
    public PageResponseDto<List<UserRolePermissionDto>> pageQueryPendingAdminApplications(
            PageRequestDto pageRequestDto) {
        Result<Record> userRecords =
                userRepository.pageFetchUserAggByRoleCode(
                        pageRequestDto, ERole.PENDING_ADMIN.name());
        if (userRecords.isEmpty()) {
            return PageResponseDto.empty();
        }
        List<UserRolePermissionDto> nestedUserAgg = userRecords.into(UserRolePermissionDto.class);
        return new PageResponseDto<>(
                userRecords.get(0).getValue("total_user", Integer.class), nestedUserAgg);
    }

    private List<Role> getCurrentRolesOrThrow(Long userId) {
        if (userRepository.fetchOneById(userId) == null) {
            throw new BusinessException("User not found with id: " + userId);
        }
        List<Long> roleIds =
                userRoleMapRepository.fetchByUserId(userId).stream()
                        .map(UserRoleMap::getRoleId)
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(roleRepository.selectByRoleIdIn(roleIds));
    }

    private Role roleByCodeOrThrow(ERole eRole) {
        Role role = roleRepository.fetchOneByCode(eRole.name());
        if (role == null) {
            throw new BusinessException("Role not initialized in database: " + eRole.name());
        }
        return role;
    }

    private void ensureRoleAbsent(List<Role> roles, ERole eRole, String message) {
        if (roles.stream().anyMatch(role -> eRole.name().equals(role.getCode()))) {
            throw new BusinessException(message);
        }
    }

    private Role ensureRolePresent(List<Role> roles, ERole eRole, String message) {
        return roles.stream()
                .filter(role -> eRole.name().equals(role.getCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(message));
    }

    /** 按角色 id 全量回绑（bindRoleToUser 为整体替换语义，保留用户已有角色） */
    private void rebindRoles(Long userId, List<Role> newRoles) {
        bindRoleToUser(userId, newRoles.stream().map(Role::getId).collect(Collectors.toList()));
    }
}
