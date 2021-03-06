package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.atguigu.gmall.pms.service.SpuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * spu信息
 *
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 14:57:07
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @PostMapping("page")
    @ApiOperation("分页查询feign")
    public ResponseVo<List<SpuEntity>> querySpuPage(@RequestBody PageParamVo pageParamVo){
        PageResultVo pageResultVo = spuService.queryPage(pageParamVo);
        List<SpuEntity> list = (List<SpuEntity>)pageResultVo.getList();
        return ResponseVo.ok(list);

    }

    @ApiOperation("spu商品信息查询")
    @GetMapping("category/{categoryId}")
    public ResponseVo<PageResultVo> querySpuInfo(@PathVariable("categoryId")Long categoryId,PageParamVo pageParamVo){
        PageResultVo pageResultVo = this.spuService.querySpuInfo(pageParamVo, categoryId);
        return ResponseVo.ok(pageResultVo);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id){
		SpuEntity spu = spuService.getById(id);

        return ResponseVo.ok(spu);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuVo spuVo) throws FileNotFoundException {
		spuService.bigSave(spuVo);

        return ResponseVo.ok("保存成功!!");
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuEntity spu){
		spuService.updateById(spu);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		spuService.removeByIds(ids);

        return ResponseVo.ok();
    }



}
