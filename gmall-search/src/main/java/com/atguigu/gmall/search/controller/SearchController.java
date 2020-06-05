package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//@RestController
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo searchResponseVo = searchService.search(searchParamVo);
        model.addAttribute("response",searchResponseVo);
        model.addAttribute("searchParam",searchParamVo);
        return "search";
    }

}
