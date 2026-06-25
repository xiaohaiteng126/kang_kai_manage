package com.kangkai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kangkai.entity.SysUser;

import java.util.List;

public interface UserService extends IService<SysUser> {
    List<SysUser> listUsers();
    SysUser getUserById(Long id);
    SysUser createUser(SysUser user);
    void updateUser(SysUser user);
    void deleteUser(Long id);
    void resetPassword(Long id, String newPassword);
}
