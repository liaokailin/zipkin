#  zipkin

![zipkin](http://zipkin.io/public/img/zipkin-logo-200x119.jpg)

`zipkin`为分布式链路调用监控系统，聚合各业务系统调用延迟数据，达到链路调用监控跟踪。


## architecture


![slow service](https://raw.githubusercontent.com/liaokailin/pic-repo/master/slow-service.png)
如图，在复杂的调用链路中假设存在一条调用链路响应缓慢，如何定位其中延迟高的服务呢？

* 日志： 通过分析调用链路上的每个服务日志得到结果
* zipkin：使用`zipkin`的`web UI`可以一眼看出延迟高的服务

![zipkin](http://zipkin.io/public/img/architecture-1.png)

如图所示，各业务系统在彼此调用时，将特定的跟踪消息传递至`zipkin`,zipkin在收集到跟踪信息后将其聚合处理、存储、展示等，用户可通过`web UI`方便
获得网络延迟、调用链路、系统依赖等等。

![zipkin](http://zipkin.io/public/img/architecture-0.png)

`zipkin`主要涉及四个组件 `collector` `storage` `search` `web UI`

*  `Collector`接收各service传输的数据
*  `Cassandra`作为`Storage`的一种，也可以是mysql等，默认存储在内存中，配置`cassandra`可以参考[这里](https://github.com/openzipkin/zipkin-dependencies)
*  `Query`负责查询`Storage`中存储的数据,提供简单的`JSON API`获取数据，主要提供给`web UI`使用
*  `Web` 提供简单的web界面


## install

执行如下命令下载jar包
```shell
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
```
其为一个`spring boot`功能，直接运行jar

```shell
nohup java -jar zipkin.jar & 
```

访问 http://ip:9411
![web-ui](https://raw.githubusercontent.com/liaokailin/pic-repo/master/zipkin-web-ui.png)

## terminology

使用`zipkin`涉及几个概念

* `Span`:基本工作单元，一次链路调用(可以是RPC，DB等没有特定的限制)创建一个`span`，通过一个64位ID标识它，
         `span`通过还有其他的数据，例如描述信息，时间戳，key-value对的(Annotation)tag信息，`parent-id`等,其中`parent-id`
	  可以表示`span`调用链路来源，通俗的理解`span`就是一次请求信息

* `Trace`:类似于树结构的`Span`集合，表示一条调用链路，存在唯一标识

* `Annotation`: 注解,用来记录请求特定事件相关信息(例如时间)，通常包含四个注解信息

    >>cs - Client Start,表示客户端发起请求
    
    >>sr - Server Receive,表示服务端收到请求
    
    >>ss - Server Send,表示服务端完成处理，并将结果发送给客户端

    >>cr - Client Received,表示客户端获取到服务端返回信息


* `BinaryAnnotation`:提供一些额外信息，一般已key-value对出现

概念说完，来看下完整的调用链路
![request chain](https://raw.githubusercontent.com/spring-cloud/spring-cloud-sleuth/master/docs/src/main/asciidoc/images/trace-id.png)
 
上图表示一请求链路，一条链路通过`Trace Id`唯一标识，`Span`标识发起的请求信息，各`span`通过`parent id` 关联起来，如图
![tree-like](https://raw.githubusercontent.com/spring-cloud/spring-cloud-sleuth/master/docs/src/main/asciidoc/images/parents.png)

整个链路的依赖关系如下:
![dependency](https://raw.githubusercontent.com/spring-cloud/spring-cloud-sleuth/master/docs/src/main/asciidoc/images/dependencies.png)

完成链路调用的记录后，如何来计算调用的延迟呢，这就需要利用`Annotation`信息

![annotation](https://raw.githubusercontent.com/liaokailin/pic-repo/master/zipkin-annotation.png)


>>sr-cs 得到请求发出延迟

>>ss-sr 得到服务端处理延迟

>>cr-cs 得到真个链路完成延迟


## brave

作为各调用链路，只需要负责将指定格式的数据发送给`zipkin`即可，利用[brave](https://github.com/openzipkin/brave)可快捷完成操作。


首先导入jar包`pom.xml`

```java
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.3.6.RELEASE</version>
    </parent>



    <!-- https://mvnrepository.com/artifact/io.zipkin.brave/brave-core -->
    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-core</artifactId>
            <version>3.9.0</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/io.zipkin.brave/brave-http -->
        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-http</artifactId>
            <version>3.9.0</version>
        </dependency>
        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-spancollector-http</artifactId>
            <version>3.9.0</version>
        </dependency>
        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-web-servlet-filter</artifactId>
            <version>3.9.0</version>
        </dependency>

        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-okhttp</artifactId>
            <version>3.9.0</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-api -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.13</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.1</version>
        </dependency>

    </dependencies>

```

利用`spring boot`创建工程

`Application.java`

```java
package com.lkl.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * Created by liaokailin on 16/7/27.
 */
@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);


    }
}

```


建立`controller`对外提供服务

`HomeController.java`

```
RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    private OkHttpClient client;

    private  Random random = new Random();

    @RequestMapping("start")
    public String start() throws InterruptedException, IOException {
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        Request request = new Request.Builder().url("http://localhost:9090/foo").get().build();
        Response response = client.newCall(request).execute();
        return " [service1 sleep " + sleep+" ms]" + response.body().toString();
    }

```


`HomeController`中利用`OkHttpClient`调用发起http请求。在每次发起请求时则需要通过`brave`记录`Span`信息，并异步传递给`zipkin`
作为被调用方(服务端)也同样需要完成以上操作.

`ZipkinConfig.java`

```java

package com.lkl.zipkin.config;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.okhttp.BraveOkHttpRequestResponseInterceptor;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by liaokailin on 16/7/27.
 */
@Configuration
public class ZipkinConfig {

    @Autowired
    private ZipkinProperties properties;


    @Bean
    public SpanCollector spanCollector() {
        HttpSpanCollector.Config config = HttpSpanCollector.Config.builder().connectTimeout(properties.getConnectTimeout()).readTimeout(properties.getReadTimeout())
                .compressionEnabled(properties.isCompressionEnabled()).flushInterval(properties.getFlushInterval()).build();
        return HttpSpanCollector.create(properties.getUrl(), config, new EmptySpanCollectorMetricsHandler());
    }


    @Bean
    public Brave brave(SpanCollector spanCollector){
        Brave.Builder builder = new Brave.Builder(properties.getServiceName());  //指定state
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        Brave brave = builder.build();
        return brave;
    }

    @Bean
    public BraveServletFilter braveServletFilter(Brave brave){
        BraveServletFilter filter = new BraveServletFilter(brave.serverRequestInterceptor(),brave.serverResponseInterceptor(),new DefaultSpanNameProvider());
        return filter;
    }

    @Bean
    public OkHttpClient okHttpClient(Brave brave){
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BraveOkHttpRequestResponseInterceptor(brave.clientRequestInterceptor(), brave.clientResponseInterceptor(), new DefaultSpanNameProvider()))
                .build();
        return client;
    }
}

```

* `SpanCollector` 配置收集器

* `Brave` 各工具类的封装,其中`builder.traceSampler(Sampler.ALWAYS_SAMPLE)`设置采样比率，0-1之间的百分比

* `BraveServletFilter` 作为拦截器，需要`serverRequestInterceptor`,`serverResponseInterceptor` 分别完成`sr`和`ss`操作

* `OkHttpClient` 添加拦截器，需要`clientRequestInterceptor`,`clientResponseInterceptor` 分别完成`cs`和`cr`操作,该功能由
brave中的`brave-okhttp`模块提供，同样的道理如果需要记录数据库的延迟只要在数据库操作前后完成`cs`和`cr`即可，当然brave提供其封装。


以上还缺少一个配置信息`ZipkinProperties.java`

```
package com.lkl.zipkin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by liaokailin on 16/7/28.
 */
@Configuration
@ConfigurationProperties(prefix = "com.zipkin")
public class ZipkinProperties {

    private String serviceName;

    private String url;

    private int connectTimeout;

    private int readTimeout;

    private int flushInterval;

    private boolean compressionEnabled;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}


```

则可以在配置文件`application.properties`中配置相关信息

```
com.zipkin.serviceName=service1
com.zipkin.url=http://110.173.14.57:9411
com.zipkin.connectTimeout=6000
com.zipkin.readTimeout=6000
com.zipkin.flushInterval=1
com.zipkin.compressionEnabled=true
server.port=8080
```

那么其中的`service1`即完成，同样的道理，修改配置文件(调整`com.zipkin.serviceName`,以及`server.port`)以及`controller`对应的方法构造若干服务


`service1` 中访问`http://localhost:8080/start`需要访问`http://localhost:9090/foo`,则构造`server2`提供该方法

`server2`配置

```
com.zipkin.serviceName=service2
com.zipkin.url=http://110.173.14.57:9411
com.zipkin.connectTimeout=6000
com.zipkin.readTimeout=6000
com.zipkin.flushInterval=1
com.zipkin.compressionEnabled=true


server.port=9090
```

`controller`方法

```java
    @RequestMapping("foo")
    public String foo() throws InterruptedException, IOException {
        Random random = new Random();
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        Request request = new Request.Builder().url("http://localhost:9091/bar").get().build();  //service3
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        request = new Request.Builder().url("http://localhost:9092/tar").get().build();  //service4
        response = client.newCall(request).execute();
       result += response.body().string();
        return " [service2 sleep " + sleep+" ms]" + result;
    }
```

在`server2`中调用`server3`和`server4`中的方法

方法分别为
```java
 @RequestMapping("bar")
    public String bar() throws InterruptedException, IOException {  //service3 method
        Random random = new Random();
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return " [service3 sleep " + sleep+" ms]";
    }

    @RequestMapping("tar")
    public String tar() throws InterruptedException, IOException { //service4 method
        Random random = new Random();
        int sleep= random.nextInt(1000);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return " [service4 sleep " + sleep+" ms]";
    }
```
将工程修改后编译成`jar`形式

执行

```shell

nohup java -jar server4.jar &
nohup java -jar server3.jar &
nohup java -jar server2.jar &
nohup java -jar server1.jar &

```
访问`http://localhost:8080/start`后查看`zipkin`的`web UI`

![chain](https://raw.githubusercontent.com/liaokailin/pic-repo/master/zipkin-service-chain.png)

点击条目可以查看具体的延迟信息

![times](https://raw.githubusercontent.com/liaokailin/pic-repo/master/zipkin-service3.png)

服务之间的依赖为
![dependency](https://raw.githubusercontent.com/liaokailin/pic-repo/master/zipkin-service-dependency.png)


more about is [here](https://github.com/liaokailin/zipkin)



