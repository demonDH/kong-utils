package com.kong.feign;

import com.kong.feign.SearchFeign.SearchFallback;
import com.kong.helper.FeignResult;
import com.kong.helper.SentinelHelper;
import feign.hystrix.FallbackFactory;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kong
 */
@FeignClient(
    name = "searchFeign",
    url = "${feign.search}",
    fallbackFactory = SearchFallback.class)
@ResponseBody
public interface SearchFeign {

    /**
     * search sth
     * @param param search param
     * @return search result
     */
    @RequestMapping("/search/")
    FeignResult<Map<String, String>> search(@RequestParam Map<String, Object> param);

    @Slf4j
    @Component
    class SearchFallback implements FallbackFactory<SearchFeign> {

        @Override
        public SearchFeign create(Throwable cause) {

            SentinelHelper.getInstance().degradeAlarm(cause);

            return param -> {
                log.info("enter search degrade strategy, param:{}", param);
                return null;
            };
        }
    }
}
