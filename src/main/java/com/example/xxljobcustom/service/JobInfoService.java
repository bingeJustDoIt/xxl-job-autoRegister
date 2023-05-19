package com.example.xxljobcustom.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.xxljobcustom.common.XxlAutoRegisterProperties;
import com.example.xxljobcustom.model.XxlJobInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class JobInfoService {
    @Resource
    private XxlAutoRegisterProperties xxlAutoRegisterProperties;
    @Resource
    private JobLoginService jobLoginService;

    /**
     * 根据执行器id，jobHandler名称查询任务列表
     * @param jobGroupId
     * @param executorHandler
     * @return
     */
    public List<XxlJobInfo> getJobInfo(Integer jobGroupId, String executorHandler) {
        String url= xxlAutoRegisterProperties.getUrl()+"/jobinfo/pageList";
        HttpResponse response = HttpRequest.post(url)
                .form("jobGroup", jobGroupId)
                .form("executorHandler", executorHandler)
                .form("triggerStatus", -1)
                .cookie(jobLoginService.getCookie())
                .execute();

        String body = response.body();
        Gson gson = new Gson();
//        JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
        JsonArray array = gson.fromJson(body, JsonObject.class).getAsJsonArray("data");

        List<XxlJobInfo> list = StreamSupport.stream(array.spliterator(),false)
                .map(o -> gson.fromJson(o,XxlJobInfo.class))
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 注册一个新任务，最终返回创建的新任务的id
     * @param xxlJobInfo
     * @return
     */
    public Integer addJobInfo(XxlJobInfo xxlJobInfo) {
        String url= xxlAutoRegisterProperties.getUrl()+"/jobinfo/add";
        Map<String, Object> paramMap = BeanUtil.beanToMap(xxlJobInfo);
        HttpResponse response = HttpRequest.post(url)
                .form(paramMap)
                .cookie(jobLoginService.getCookie())
                .execute();
        Gson gson = new Gson();
//        JSON json = JSONUtil.parse(response.body());
        JsonObject body = gson.fromJson(response.body(), JsonObject.class);
        Integer code = body.get("code").getAsInt();
        if (code.equals(200)){
            return body.get("content").getAsInt();
        }
        throw new RuntimeException("add jobInfo error!");
    }


    public void startJob(Integer id){
        String url= xxlAutoRegisterProperties.getUrl()+"/jobinfo/start";
        HttpResponse response = HttpRequest.post(url)
                .form("id",id)
                .cookie(jobLoginService.getCookie())
                .execute();
        Gson gson = new Gson();
//        JSON json = JSONUtil.parse(response.body());
        JsonObject body = gson.fromJson(response.body(), JsonObject.class);
        Integer code = body.get("code").getAsInt();
        if (code.equals(200)){
            log.info("startJob:{} success",id);
            return;
        }
        throw new RuntimeException("startJob jobInfo error!");
    }
    public void stopJob(Integer id){
        String url= xxlAutoRegisterProperties.getUrl()+"/jobinfo/stop";
        HttpResponse response = HttpRequest.post(url)
                .form("id",id)
                .cookie(jobLoginService.getCookie())
                .execute();
        Gson gson = new Gson();
//        JSON json = JSONUtil.parse(response.body());
        JsonObject body = gson.fromJson(response.body(), JsonObject.class);
        Integer code = body.get("code").getAsInt();
        if (code.equals(200)){
            log.info("stopJob:{} success",id);
        }
        throw new RuntimeException("stopJob jobInfo error!");
    }
}
