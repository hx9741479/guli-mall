package com.atguigu.elasticsearchdemo.repository;

import com.atguigu.elasticsearchdemo.pojo.User;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchRepository<User,Long> {

    /**
     * 根据年龄区间查询
     * @param age1
     * @param age2
     * @return
     */
    List<User> findByAgeBetween(Integer age1, Integer age2);

    @Query(
            " {\n" +
                    "    \"range\": {\n" +
                    "      \"age\": {\n" +
                    "        \"gte\": \"?0\",\n" +
                    "        \"lte\": \"?1\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }"
    )
    List<User> findByQuery(Integer age1,Integer age2);

    //List<User>

}
