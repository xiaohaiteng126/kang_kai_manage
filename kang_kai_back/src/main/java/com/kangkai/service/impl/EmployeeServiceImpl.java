package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kangkai.common.BusinessException;
import com.kangkai.entity.Employee;
import com.kangkai.entity.WorkDay;
import com.kangkai.mapper.EmployeeMapper;
import com.kangkai.mapper.WorkDayMapper;
import com.kangkai.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Resource
    private WorkDayMapper workDayMapper;

    @Override
    public Page<Employee> queryPage(Page<Employee> page, Long projectId, String name, String phone, String idCard) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getProjectId, projectId);
        if (StringUtils.hasText(name)) {
            wrapper.like(Employee::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Employee::getPhone, phone);
        }
        if (StringUtils.hasText(idCard)) {
            wrapper.like(Employee::getIdCard, idCard);
        }
        wrapper.orderByDesc(Employee::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean saveEmployee(Employee employee) {
        validateEmployee(employee);
        if (employee.getStatus() == null) {
            employee.setStatus("在项");
        }
        return this.save(employee);
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        validateEmployee(employee);
        return this.updateById(employee);
    }

    @Override
    public boolean deleteEmployee(Long id) {
        Long count = workDayMapper.selectCount(
                new LambdaQueryWrapper<WorkDay>().eq(WorkDay::getEmployeeId, id));
        if (count > 0) {
            throw new BusinessException("该员工存在工日记录，无法删除");
        }
        return this.removeById(id);
    }

    private void validateEmployee(Employee employee) {
        if (!StringUtils.hasText(employee.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        if (employee.getDailyWage() == null || employee.getDailyWage().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("日资必须大于0");
        }
        if (!StringUtils.hasText(employee.getGender()) ||
                (!"男".equals(employee.getGender()) && !"女".equals(employee.getGender()))) {
            throw new BusinessException("性别必须为男或女");
        }
        if (employee.getProjectId() == null) {
            throw new BusinessException("所属项目不能为空");
        }
    }
}
