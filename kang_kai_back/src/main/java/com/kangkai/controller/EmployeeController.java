package com.kangkai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kangkai.common.Result;
import com.kangkai.entity.Employee;
import com.kangkai.service.EmployeeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping
    public Result<Page<Employee>> list(@RequestParam Long projectId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String idCard,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "1000") Integer pageSize) {
        Page<Employee> page = new Page<>(pageNum, pageSize);
        return Result.success(employeeService.queryPage(page, projectId, name, phone, idCard));
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PostMapping
    public Result<Employee> create(@RequestBody Employee employee) {
        employeeService.saveEmployee(employee);
        return Result.success(employee);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        employeeService.updateEmployee(employee);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success("删除成功");
    }
}
