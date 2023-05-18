package com.example.xxljobcustom.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.xxljobcustom.common.XxlAutoRegisterProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

@Service
public class JobLoginService {
    @Resource
    private XxlAutoRegisterProperties xxlAutoRegisterProperties;
    private final Map<String, String> loginCookie = new HashMap<>();

    public void login() {
     /*   String url=adminAddresses+"/login";
        HttpResponse response = HttpRequest.post(url)
                .form("userName",username)
                .form("password",password)
                .execute();
        List<HttpCookie> cookies = response.getCookies();
        Optional<HttpCookie> cookieOpt = cookies.stream()
                .filter(cookie -> cookie.getName().equals("XXL_JOB_LOGIN_IDENTITY")).findFirst();
        if (!cookieOpt.isPresent())
            throw new RuntimeException("get xxl-job cookie error!");

        String value = cookieOpt.get().getValue();
        loginCookie.put("XXL_JOB_LOGIN_IDENTITY",value);*/
        String url = xxlAutoRegisterProperties.getUrl() + "/login";
        HashMap<String, Object> body = new HashMap<String, Object>() {{
            put("userName", xxlAutoRegisterProperties.getUsername());
            put("password", xxlAutoRegisterProperties.getPassword());
        }};
        HttpResponse httpResponse = HttpRequest.post(url).form(body).execute();
        HttpCookie httpCookie = httpResponse.getCookie("XXL_JOB_LOGIN_IDENTITY");
        String value = httpCookie.getValue();
        loginCookie.put("XXL_JOB_LOGIN_IDENTITY", value);
    }
    public String getCookie() {
        for (int i = 0; i < 3; i++) {
            String cookieStr = loginCookie.get("XXL_JOB_LOGIN_IDENTITY");
            if (cookieStr !=null) {
                return "XXL_JOB_LOGIN_IDENTITY="+cookieStr;
            }
            login();
        }
        throw new RuntimeException("get xxl-job cookie error!");
    }
}
