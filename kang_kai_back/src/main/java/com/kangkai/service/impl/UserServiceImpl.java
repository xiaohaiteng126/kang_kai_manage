package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kangkai.common.BusinessException;
import com.kangkai.entity.SysUser;
import com.kangkai.mapper.SysUserMapper;
import com.kangkai.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    private void checkAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !"kang".equals(auth.getName())) {
            throw new AccessDeniedException("仅管理员可操作");
        }
    }

    @Override
    public List<SysUser> listUsers() {
        checkAdmin();
        return this.list().stream().peek(u -> u.setPassword(null)).collect(Collectors.toList());
    }

    @Override
    public SysUser getUserById(Long id) {
        checkAdmin();
        SysUser user = this.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public SysUser createUser(SysUser user) {
        checkAdmin();
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        // Check username uniqueness
        Long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUser(SysUser user) {
        checkAdmin();
        // Do NOT update password here
        user.setPassword(null);
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        checkAdmin();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser currentUser = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, auth.getName()));
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new BusinessException("不能删除自己");
        }
        this.removeById(id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        checkAdmin();
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }
}
