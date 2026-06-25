package com.kangkai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kangkai.entity.Project;

public interface ProjectService extends IService<Project> {
    boolean deleteProject(Long id);
}
