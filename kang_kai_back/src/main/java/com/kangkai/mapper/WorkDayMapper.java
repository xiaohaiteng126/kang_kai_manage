package com.kangkai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kangkai.entity.WorkDay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkDayMapper extends BaseMapper<WorkDay> {

    void upsert(@Param("list") List<WorkDay> list);
}
