# 魔改xxl-job 添加自动注册功能
打包命令：mvn clean install -DskipTests

示例：
只需要同时使用原生@XxlJob注解，加上自定义的@XxlRegister注解，即可自动将此任务有关信息注册到xxl-job管理中心，
当然，使用方还需要添加有关信息
# 使用步骤
0. 将本项目打包并install：  mvn clean install -DskipTests
1. 客户端引入依赖
```xml
     <dependency>
            <groupId>com.example</groupId>
            <artifactId>xxl-job-custom</artifactId>
            <version>0.0.1-SNAPSHOT</version>
     </dependency>
```

2. 客户端配置
```yaml
# Xxl-job 配置
xxl:
  job:
    admin:
      # admin地址 目前仅支持单点
      addresses: http://localhost:8081/xxl-job-admin
      # admin 账号密码 用于拿到cookie方便后续的自动注册
      username: admin
      password: 123456
    # admin token
    accessToken: default_token
    executor:
      # 本实例对应的执行器AppName 用于多实例部署
      appName: xxl-test-executor
      # 本实例对应的执行器Title 区分本实例和其他实例
      title: 示例执行器

```

配置类示例：
```java
@Configuration
public class XxlJobConfig {
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

//    @Value("${xxl.job.executor.address}")
//    private String address;
//
//    @Value("${xxl.job.executor.ip}")
//    private String ip;
//
//    @Value("${xxl.job.executor.port:0}")
//    private int port;

//    @Value("${xxl.job.executor.logpath}")
//    private String logPath;

    @Value("${xxl.job.executor.logretentiondays:30}")
    private int logRetentionDays;


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(notNull(adminAddresses));
        xxlJobSpringExecutor.setAppname(notNull(appname));
        xxlJobSpringExecutor.setAddress(null);
        xxlJobSpringExecutor.setIp(null);
        xxlJobSpringExecutor.setPort(0);
        xxlJobSpringExecutor.setAccessToken(notNull(accessToken));
        xxlJobSpringExecutor.setLogPath(null);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }

    @PostConstruct
    public void init() {
        System.out.println();
    }


    public String notNull(String str){
        if (ObjectUtils.isEmpty(str)){
            return null;
        }
        return str;
    }




    /**
     * 针对多网卡、容器内部署等情况，可借助 "spring-cloud-commons" 提供的 "InetUtils" 组件灵活定制注册IP；
     *
     *      1、引入依赖：
     *          <dependency>
     *             <groupId>org.springframework.cloud</groupId>
     *             <artifactId>spring-cloud-commons</artifactId>
     *             <version>${version}</version>
     *         </dependency>
     *
     *      2、配置文件，或者容器启动变量
     *          spring.cloud.inetutils.preferred-networks: 'xxx.xxx.xxx.'
     *
     *      3、获取IP
     *          String ip_ = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
     */
}
```


3. 添加自定义的注解 补充有关任务信息
```java
   @XxlJob(value = "testJob")
    @XxlRegister(cron = "0 0 0 * * ? *",
            author = "binge",
            jobDesc = "测试job")
    public void testJob(){
        System.out.println("hello world");
    }
```