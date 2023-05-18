# 魔改xxl-job 添加自动注册功能
示例：
只需要同时使用原生@XxlJob注解，加上自定义的@XxlRegister注解，即可自动将此任务有关信息注册到xxl-job管理中心，
当然，使用方还需要添加有关信息
```java
   @XxlJob(value = "testJob")
    @XxlRegister(cron = "0 0 0 * * ? *",
            author = "binge",
            jobDesc = "测试job")
    public void testJob(){
        System.out.println("hello world");
    }
```