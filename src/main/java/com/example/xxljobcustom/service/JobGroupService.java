package com.example.xxljobcustom.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.xxljobcustom.common.XxlAutoRegisterProperties;
import com.example.xxljobcustom.model.XxlJobGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class JobGroupService {
    @Resource
    private XxlAutoRegisterProperties xxlAutoRegisterProperties;
    @Resource
    private JobLoginService jobLoginService;
    /**
     * 根据appName和执行器名称title查询执行器列表
     * @return
     */
    public List<XxlJobGroup> getJobGroup(String appName,String title) {
        String url= xxlAutoRegisterProperties.getUrl()+"/jobgroup/pageList";
       /* HttpResponse response = HttpRequest.post(url)
                .form("appname", appName)
                .form("title", title)
                .cookie(jobLoginService.getCookie())
                .execute();

        String body = response.body();
        JSONArray array = JSONUtil.parse(body).getByPath("data", JSONArray.class);
        List<XxlJobGroup> list = array.stream()
                .map(o -> JSONUtil.toBean((JSONObject) o, XxlJobGroup.class))
                .collect(Collectors.toList());*/
        HttpResponse httpResponse = HttpRequest.post(url).form("appname", appName).form("title", title).cookie(jobLoginService.getCookie()).execute();
        String body = httpResponse.body();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        JsonArray array = jsonObject.get("data").getAsJsonArray();
        List<XxlJobGroup> list = StreamSupport.stream(array.spliterator(), false).map(x -> {
            return gson.fromJson(x,XxlJobGroup.class);
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * 判断当前执行器是否已经被注册到调度中心过
     * @param appName
     * @param title
     * @return
     */
    public boolean preciselyCheck(String appName,String title) {
        List<XxlJobGroup> jobGroup = getJobGroup(appName,title);
        Optional<XxlJobGroup> has = jobGroup.stream()
                .filter(xxlJobGroup -> xxlJobGroup.getAppname().equals(appName) && xxlJobGroup.getTitle().equals(title))
                .findAny();
        return has.isPresent();
    }

    /**
     * 注册新executor到调度中心：
     * @param appName
     * @param title
     * @return
     */
    public boolean autoRegisterGroup(String appName,String title) {
        String url= xxlAutoRegisterProperties.getUrl()+"/jobgroup/save";
        HttpResponse response = HttpRequest.post(url)
                .form("appname", appName)
                .form("title", title)
                .cookie(jobLoginService.getCookie())
                .execute();
        Gson gson = new Gson();
        Integer code = gson.fromJson(response.body(),JsonObject.class).get("code").getAsInt();
        return code.equals(200);
    }
}
