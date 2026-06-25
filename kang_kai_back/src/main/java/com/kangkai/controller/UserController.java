package com.kangkai.controller;

import com.kangkai.common.Result;
import com.kangkai.dto.ResetPasswordRequest;
import com.kangkai.entity.SysUser;
import com.kangkai.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    public Result<List<SysUser>> list() {
        return Result.success(userService.listUsers());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody SysUser user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return Result.success("密码重置成功");
    }
}
