package com.zl.mjga.model.urp;

public enum ERole {
    ADMIN,
    GENERAL,
    /** 管理员申请标记角色：申请中绑定，审批通过置换为 ADMIN，驳回移除 */
    PENDING_ADMIN
}
