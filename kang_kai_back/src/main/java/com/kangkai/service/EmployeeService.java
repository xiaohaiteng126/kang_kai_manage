package com.kangkai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kangkai.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    Page<Employee> queryPage(Page<Employee> page, Long projectId, String name, String phone, String idCard);
    boolean saveEmployee(Employee employee);
    boolean updateEmployee(Employee employee);
    boolean deleteEmployee(Long id);
}
