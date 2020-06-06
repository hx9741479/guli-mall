package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @ResponseBody
    @GetMapping("/index/read")
    public ResponseVo<String> read(){
        String msg = indexService.readLock();

        return ResponseVo.ok(msg);
    }

    @ResponseBody
    @GetMapping("/index/write")
    public ResponseVo<String> write(){
        String msg = indexService.writeLock();

        return ResponseVo.ok(msg);
    }

    @GetMapping("/index/cates")
    public String toIndex(Model model){

        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);

        // TODO: 加载其他数据

        return "index";
    }

    /**
     *  获取二级分类以及二级分类下的三级分类
     * @param pid
     * @return
     */
    @ResponseBody
    @GetMapping("/index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSub(@PathVariable("pid")Long pid){
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);
    }


}
