package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import org.springframework.stereotype.Service;

@Service
public interface SearchService {

    SearchResponseVo search(SearchParamVo searchParamVo);

}
