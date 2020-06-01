package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseAttrVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            // 构建查询条件
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.builderDsl(searchParamVo));
            // 执行查询
            SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析结果集
            SearchResponseVo searchResponseVo = this.parseResult(searchResponse);
            //分页数据
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析搜索结果集
     *
     * @param response
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = response.getHits();

        // 分页数据中 total 命中的记录数
        searchResponseVo.setTotal(hits.getTotalHits());
        // 当前页数据
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            // 获取内层hits的_source 数据
            String goodsJson = hitsHit.getSourceAsString();
            // _source数据反序列化为goods对象
            Goods goods = JSON.parseObject(goodsJson, Goods.class);
            // 获取高亮的title覆盖掉普通title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField titleHighlightField = highlightFields.get("title");
            String hightlightTitle = titleHighlightField.getFragments()[0].toString();
            goods.setTitle(hightlightTitle);
            return goods;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        // 聚合结果集的解析
        Map<String, Aggregation> stringAggregationMap = response.getAggregations().asMap();
        //过滤
        //品牌聚合数据
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)stringAggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        //将品牌桶转化为品牌集合
        List<BrandEntity> brands = buckets.stream().map(bucket -> {
            BrandEntity brandEntity = new BrandEntity();
            //设置品牌id
            long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            brandEntity.setId(brandId);
            //获取桶中子聚合
            Map<String, Aggregation> brandAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            // 解析品牌名称的子聚合，获取品牌名称
            ParsedStringTerms brandNameAgg = (ParsedStringTerms)brandAggregationMap.get("brandNameAgg");
            //一个品牌名对应的名肯定只有一个
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandEntity.setName(brandName);
            // 解析品牌log的子聚合，获取log
            Map<String, Aggregation> brandLogAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            ParsedStringTerms logoAgg = (ParsedStringTerms)brandLogAggregationMap.get("logoAgg");
            //一个品牌只有一个logo
            brandEntity.setLogo(logoAgg.getBuckets().get(0).getKeyAsString());

            return brandEntity;
        }).collect(Collectors.toList());
        searchResponseVo.setBrands(brands);
        //分类聚合数据
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)stringAggregationMap.get("categoryIdAgg");
        //获取分类聚合的桶
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        List<CategoryEntity> categories = categoryIdAggBuckets.stream().map(bucket -> {
            CategoryEntity categoryEntity = new CategoryEntity();
            //设置分类Id
            long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            categoryEntity.setId(categoryId);
            //设置分类名称
            //获取子聚合
            ParsedStringTerms categoryNameAgg = ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            categoryEntity.setName(categoryName);
            return categoryEntity;
        }).collect(Collectors.toList());
        searchResponseVo.setCategories(categories);

        // 解析聚合结果集，获取规格参数 注意这个地方是嵌套聚合 先获取嵌套聚合
        ParsedNested attrAgg = (ParsedNested)stringAggregationMap.get("attrAgg");
        //获取参数聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrAggBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(attrAggBuckets)){
            List<SearchResponseAttrVo> searchResponseAttrVos = attrAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //设置规格Id
                long attrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);
                //设置规格名称
                //获取规格名称子聚合
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //设置规格参数值集合
                //获取规格参数子聚合
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrValueAggBuckets)){
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValues(attrValues);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setFilters(searchResponseAttrVos);
        }

        return searchResponseVo;
    }

    /**
     * 构建 Dsl 语句
     *
     * @param searchParamVo
     * @return
     */
    private SearchSourceBuilder builderDsl(SearchParamVo searchParamVo) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = searchParamVo.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            // 打广告，TODO
            return null;
        }

        // 1. 构建查询条件（bool查询）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1. 匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        // 1.2. 过滤
        // 1.2.1. 品牌过滤
        List<Long> brandId = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        // 1.2.2. 分类过滤
        Long cid = searchParamVo.getCid();
        if (cid != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", cid));
        }
        // 1.2.3. 价格区间过滤
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
        }
        // 1.2.4. 是否有货
        Boolean store = searchParamVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        // 1.2.5. 规格参数的过滤 props=5:高通-麒麟,6:骁龙865-硅谷1000
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] atts = prop.split(":");
                if (atts != null && atts.length == 2) {
                    String attrId = atts[0];
                    String[] attrValues = StringUtils.split(atts[1], "-");
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }
        sourceBuilder.query(boolQueryBuilder);
        // 2. 构建排序 0-默认，得分降序；1-按价格降序；2-按价格升序；3-按创建时间降序；4-按销量降序
        Integer sort = searchParamVo.getSort();
        String field = "";
        SortOrder order = null;
        switch (sort) {
            case 1:
                field = "price";
                order = SortOrder.DESC;
                break;
            case 2:
                field = "price";
                order = SortOrder.ASC;
                break;
            case 3:
                field = "createTime";
                order = SortOrder.DESC;
                break;
            case 4:
                field = "sales";
                order = SortOrder.DESC;
                break;
            default:
                field = "_score";
                order = SortOrder.DESC;
                break;
        }
        sourceBuilder.sort(field, order);
        // 3. 构建分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 4. 构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red'>").postTags("</font>"));
        // 5. 构建聚合
        // 5.1. 构建品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
        // 5.2. 构建分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        // 5.3. 构建规格参数的嵌套聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );
        // 6. 构建结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "price", "defaultImage","subTitle"}, null);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
