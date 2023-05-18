package com.example.xxljobcustom;

import com.example.xxljobcustom.annotation.XxlRegister;
import com.example.xxljobcustom.common.XxlAutoRegisterProperties;
import com.example.xxljobcustom.model.XxlJobGroup;
import com.example.xxljobcustom.model.XxlJobInfo;
import com.example.xxljobcustom.service.JobGroupService;
import com.example.xxljobcustom.service.JobInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自动注册执行器和jobHandler
 */
@Component
@Slf4j
@EnableConfigurationProperties(XxlAutoRegisterProperties.class) //告诉Spring Boot要扫描并注册配置属性类（被@ConfigurationProperties注解的类），以便将配置文件中的属性值绑定到这些类的实例中
@ComponentScan(basePackages = "com.example.xxljobcustom") //确保XxlAutoRegisterProperties可以被扫描到
public class XxlJobAutoRegister implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private JobGroupService jobGroupService;
    @Autowired
    private JobInfoService jobInfoService;
    @Resource
    private XxlAutoRegisterProperties xxlAutoRegisterProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        addJobGroup();//注册执行器
        addJobInfo();//注册任务
    }

    /**
     * 通过applicationContext拿到spring容器中的所有bean，再拿到这些bean中所有添加了@XxlJob注解的方法
     * 对上面获取到的方法进行检查，是否添加了我们自定义的@XxlRegister注解，如果没有则跳过，不进行自动注册
     * 对同时添加了@XxlJob和@XxlRegister的方法，通过执行器id和jobHandler的值判断是否已经在调度中心注册过了，如果已存在则跳过
     * 对于满足注解条件且没有注册过的jobHandler，调用接口注册到调度中心
     */
    private void addJobInfo() {
        List<XxlJobGroup> jobGroups = jobGroupService.getJobGroup(xxlAutoRegisterProperties.getAppName(), xxlAutoRegisterProperties.getTitle());
        XxlJobGroup xxlJobGroup = jobGroups.get(0);
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Map<Method, XxlJob> annotatedMethods  = MethodIntrospector.selectMethods(bean.getClass(),
                    new MethodIntrospector.MetadataLookup<XxlJob>() {
                        @Override
                        public XxlJob inspect(Method method) {
                            return AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                        }
                    });
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();

                //同时含有@XxlJob和@XxlRegister的 handler, 自动注册
                if (executeMethod.isAnnotationPresent(XxlRegister.class)) {
                    XxlRegister xxlRegister = executeMethod.getAnnotation(XxlRegister.class);
                    List<XxlJobInfo> jobInfo = jobInfoService.getJobInfo(xxlJobGroup.getId(), xxlJob.value());
                    if (!jobInfo.isEmpty()){
                        //因为是模糊查询，需要再判断一次
                        Optional<XxlJobInfo> first = jobInfo.stream()
                                .filter(xxlJobInfo -> xxlJobInfo.getExecutorHandler().equals(xxlJob.value()))
                                .findFirst();
                        if (first.isPresent()) {
                            continue;
                        }
                    }

                    XxlJobInfo xxlJobInfo = createXxlJobInfo(xxlJobGroup, xxlJob, xxlRegister);
                    Integer jobInfoId = jobInfoService.addJobInfo(xxlJobInfo);
                }
            }
        }
    }

    private XxlJobInfo createXxlJobInfo(XxlJobGroup xxlJobGroup, XxlJob xxlJob, XxlRegister xxlRegister){
        XxlJobInfo xxlJobInfo=new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobGroup.getId());
        xxlJobInfo.setJobDesc(xxlRegister.jobDesc());
        xxlJobInfo.setAuthor(xxlRegister.author());
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(xxlRegister.cron());
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setExecutorHandler(xxlJob.value());
        xxlJobInfo.setExecutorRouteStrategy(xxlRegister.executorRouteStrategy());
        xxlJobInfo.setMisfireStrategy("DO_NOTHING");
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);
        xxlJobInfo.setGlueRemark("GLUE代码初始化");
        xxlJobInfo.setTriggerStatus(xxlRegister.triggerStatus());

        return xxlJobInfo;
    }

    /**
     * 根据配置文件中的appName和title精确匹配查看调度中心是否已有执行器被注册过了，如果存在则跳过，不存在则新注册一个：
     */
    private void addJobGroup() {
        if (jobGroupService.preciselyCheck(xxlAutoRegisterProperties.getAppName(), xxlAutoRegisterProperties.getTitle())) {
            return;
        }

        if (jobGroupService.autoRegisterGroup(xxlAutoRegisterProperties.getAppName(), xxlAutoRegisterProperties.getTitle())) {
            log.info("auto register xxl-job group success!");
        }else {
            log.error("auto register xxl-job fail");
        }

    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
