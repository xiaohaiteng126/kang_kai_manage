package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kangkai.common.BusinessException;
import com.kangkai.entity.Employee;
import com.kangkai.entity.Project;
import com.kangkai.mapper.EmployeeMapper;
import com.kangkai.mapper.ProjectMapper;
import com.kangkai.service.ProjectService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public boolean save(Project project) {
        checkDuplicateName(project.getProjectName(), null);
        return super.save(project);
    }

    @Override
    public boolean updateById(Project project) {
        checkDuplicateName(project.getProjectName(), project.getId());
        return super.updateById(project);
    }

    @Override
    public boolean deleteProject(Long id) {
        Long count = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getProjectId, id));
        if (count > 0) {
            throw new BusinessException("项目下存在员工，无法删除");
        }
        return this.removeById(id);
    }

    private void checkDuplicateName(String name, Long excludeId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getProjectName, name);
        if (excludeId != null) {
            wrapper.ne(Project::getId, excludeId);
        }
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("项目名称已存在");
        }
    }
}
