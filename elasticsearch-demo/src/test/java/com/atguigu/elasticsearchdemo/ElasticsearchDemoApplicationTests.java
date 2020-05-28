package com.atguigu.elasticsearchdemo;

import com.atguigu.elasticsearchdemo.pojo.User;
import com.atguigu.elasticsearchdemo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class ElasticsearchDemoApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    UserRepository repository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testEnd(){
        try {
            //构建查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchQuery("name","冰冰小鹿范冰冰").operator(Operator.OR));
            sourceBuilder.sort("age",SortOrder.DESC);
            sourceBuilder.from(1).size(3);
            sourceBuilder.highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em"));
            sourceBuilder.aggregation(AggregationBuilders.terms("passwordAgg").field("password"));
            System.out.println(sourceBuilder);
            SearchRequest searchRequest = new SearchRequest(new String[]{"user"}, sourceBuilder);
            SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(searchResponse.toString());
            //搜索结果集hits
            SearchHits hitsAll = searchResponse.getHits();
            System.out.println("hits: " + hitsAll);
            SearchHit[] hits = hitsAll.getHits();
            System.out.println("命中记录数：" + hitsAll.getTotalHits());
            for (SearchHit hit: hits) {
                User user = MAPPER.readValue(hit.getSourceAsString(), User.class);
                System.out.println("普通结果集：" + user);
                //拿到高亮结果集覆盖普通结果集
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                user.setName(name.getFragments()[0].string());
                System.out.println("高亮结果集：" + user);
            }
            Map<String, Aggregation> stringAggregationMap = searchResponse.getAggregations().asMap();
            ParsedStringTerms passwordAgg = (ParsedStringTerms)stringAggregationMap.get("passwordAgg");
            passwordAgg.getBuckets().forEach(bucket -> {
                //获取桶内的key
                System.out.println("bucket.getKey(): " + bucket.getKey());
                System.out.println("聚合桶中的key: " + bucket.getKeyAsString());

            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    void testRestClient(){
        try {
            // 构建查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchQuery("userName", "冰冰"));
            sourceBuilder.sort(SortBuilders.fieldSort("age").order(SortOrder.DESC));
            sourceBuilder.from(2);
            sourceBuilder.size(2);
            sourceBuilder.highlighter(new HighlightBuilder().field("userName").preTags("<em>").postTags("</em>"));
            sourceBuilder.aggregation(AggregationBuilders.terms("passwordAgg").field("password"));
            SearchRequest searchRequest = new SearchRequest(new String[]{"user"}, sourceBuilder);
            SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析搜索结果集
            System.out.println(searchResponse.toString());
            SearchHits hits = searchResponse.getHits();
            System.out.println("命中的记录数：" + hits.getTotalHits());
            SearchHit[] hitses = hits.getHits();
            for (SearchHit hit : hitses) {
                User user = MAPPER.readValue(hit.getSourceAsString(), User.class);
                System.out.println("普通记录内容：" + user.toString());

                // 拿到高亮结果集，覆盖普通用户名
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlightField = highlightFields.get("userName");
                user.setName(highlightField.getFragments()[0].string());
                System.out.println("高亮记录内容：" + user.toString());
            }

            Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
            ParsedStringTerms passwordAgg = (ParsedStringTerms)aggregationMap.get("passwordAgg");
            passwordAgg.getBuckets().forEach(bucket -> {
                System.out.print("聚合桶中的key：");
                System.out.println(bucket.getKeyAsString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test1(){
        //创建索引
        //restTemplate.createIndex(User.class);
        ////创建映射
        //restTemplate.putMapping(User.class);
        //基本的查询
        //this.repository.save(new User(17l, "柳岩1", 201, "123456"));
        //this.repository.saveAll(Arrays.asList(
        //        new User(9l, "柳岩1", 201, "123456"),
        //        new User(10l, "小鹿1", 211, "123456"),
        //        new User(11l, "马蓉1", 221, "123456"),
        //        new User(12l, "范冰冰1", 231, "123456"),
        //        new User(13l, "李冰冰1", 241, "123456"),
        //        new User(14l, "王冰冰1", 251, "654321"),
        //        new User(15l, "陈冰冰1", 261, "654321"),
        //        new User(16l, "向冰冰1", 271, "654321")
        //));


        //NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name","冰冰"));
        //nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.ASC));
        //nativeSearchQueryBuilder.withPageable(PageRequest.of(1,2));
        //
        //nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("passwordAgg").field("password"));
        //AggregatedPage<User> userPage = (AggregatedPage)this.repository.search(nativeSearchQueryBuilder.build());
        //System.out.println(userPage.getContent());
        //System.out.println(userPage.getTotalPages());
        //System.out.println(userPage.getTotalElements());
        ////ParsedStringTerms passwordAgg = (ParsedStringTerms)userPage.getAggregation("passwordAgg");
        ////passwordAgg.getBuckets().forEach(bucket -> {
        ////    System.out.print("聚合桶中的key：");
        ////    System.out.println(bucket.getKeyAsString());
        ////});
        //nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("passwordAgg").field("password"));
        //
        //ParsedStringTerms passwordAgg = (ParsedStringTerms)userPage.getAggregation("passwordAgg");
        //passwordAgg.getBuckets().forEach(bucket -> {
        //    System.out.println("聚合桶中的key: ");
        //    System.out.println(bucket.getKeyAsString());
        //});

        // 自定义查询构建器，
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 查询条件
        //queryBuilder.withQuery(QueryBuilders.matchQuery("name", "冰冰"));
        // 排序条件
        queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC));
        // 分页条件，页码是从0开始的，
        queryBuilder.withPageable(PageRequest.of(1, 2));
        // 聚合条件
        queryBuilder.addAggregation(AggregationBuilders.terms("passwordAgg").field("password"));
        queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));

        //AggregatedPage<User> userPage = (AggregatedPage)this.repository.search(queryBuilder.build());
        //System.out.println(userPage.getContent());
        //System.out.println(userPage.getTotalPages());
        //System.out.println(userPage.getTotalElements());
        ////userPage.g
        //ParsedStringTerms passwordAgg = (ParsedStringTerms)userPage.getAggregation("passwordAgg");
        //passwordAgg.getBuckets().forEach(bucket -> {
        //    System.out.print("聚合桶中的key：");
        //    System.out.println(bucket.getKey());
        //    System.out.println(bucket.getDocCount());
        //});


    }

}
