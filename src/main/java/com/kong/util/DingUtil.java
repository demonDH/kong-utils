package com.kong.util;

import com.alibaba.csp.ahas.shaded.com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonProperty;
import com.alibaba.csp.ahas.shaded.com.alibaba.acm.shaded.org.codehaus.jackson.map.ObjectMapper;
import com.kong.config.BaseConfig;
import com.kong.context.BaseContext;
import java.io.IOException;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 * @author kong
 */
@Slf4j
public class DingUtil {

    private DingUtil() {}

    private static ObjectMapper mapper = new ObjectMapper();

    public static void push(final String webhook, final String content) {

        BaseConfig baseConfig = BaseContext.getBean(BaseConfig.class);
        if (!baseConfig.isDing()) {
            return;
        }

        DingVO dingVO = new DingVO();
        dingVO.setContent(content);
        String textMsg;
        try {
            textMsg = mapper.writeValueAsString(dingVO);
        } catch (Exception e) {
            textMsg = "json encode failed...";
        }

        StringEntity se = new StringEntity(textMsg, "utf-8");

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(webhook);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        httppost.setEntity(se);

        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            log.info("ding push fail, msg:{}", textMsg, e);
        }

        if (Objects.isNull(response) || response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.info("ding push fail, msg:{}", textMsg);
        }
    }

    @Getter
    @Setter
    private static class ContentVO {
        @JsonProperty
        private String content;
    }

    @Getter
    @Setter
    private static class AtVO {
        @JsonProperty
        private boolean isAtAll = true;
    }

    @Getter
    @Setter
    private static class DingVO {
        @JsonProperty
        private String msgtype = "text";
        @JsonProperty
        private ContentVO text = new ContentVO();
        @JsonProperty
        private AtVO at = new AtVO();

        void setContent(String content) {
            text.setContent(content);
        }
    }
}
