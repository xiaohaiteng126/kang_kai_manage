package com.kangkai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kangkai.common.BusinessException;
import com.kangkai.common.Result;
import com.kangkai.dto.ChangePasswordRequest;
import com.kangkai.dto.LoginRequest;
import com.kangkai.entity.SysUser;
import com.kangkai.mapper.SysUserMapper;
import com.kangkai.security.JwtTokenProvider;
import com.kangkai.vo.LoginVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        String token = jwtTokenProvider.generateToken(userDetails, user.getId(), user.getRealName());
        return Result.success(new LoginVO(token, user.getId(), user.getUsername(), user.getRealName()));
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码不正确");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(updateUser);
        return Result.success("密码修改成功");
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, auth.getName()));
        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        return Result.success(info);
    }
}
