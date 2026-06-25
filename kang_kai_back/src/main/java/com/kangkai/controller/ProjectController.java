package com.kangkai.controller;

import com.kangkai.common.Result;
import com.kangkai.entity.Project;
import com.kangkai.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Resource
    private ProjectService projectService;

    @GetMapping
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @PostMapping
    public Result<Project> create(@RequestBody Project project) {
        projectService.save(project);
        return Result.success(project);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectService.updateById(project);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success("删除成功");
    }
}
