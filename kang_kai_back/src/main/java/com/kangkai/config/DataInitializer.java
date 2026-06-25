package com.kangkai.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kangkai.entity.SysUser;
import com.kangkai.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<>());
        if (count == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("kang");
            admin.setPassword(passwordEncoder.encode("kang123"));
            admin.setRealName("康之凯");
            admin.setEnabled(1);
            sysUserMapper.insert(admin);
            System.out.println(">>> 默认管理员用户已创建: kang / kang123");
        }
    }
}
