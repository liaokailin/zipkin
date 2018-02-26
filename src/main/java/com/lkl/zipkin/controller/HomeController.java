package com.lkl.zipkin.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by liaokailin on 16/7/27.
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    private OkHttpClient client;

    private  Random random = new Random();

    @RequestMapping("/")
    public String home() throws InterruptedException, IOException {
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br>[home sleep " + sleep +" ms]" +
            "<br><br><a href='/service1'>service1</a>" +
            "<br><br><a href='/service2'>service2</a><br>-> service1" +
            "<br><br><a href='/service3'>service3</a><br>-> service2 -> service1" +
            "<br><br><a href='/service4'>service4</a><br>-> service3 -> service2 -> service1" +
            "<br><br><a href='/service5'>service5</a><br>-> service3 -> service2 -> service1<br>-> service2 -> service1";
    }

    @RequestMapping("service1")
    public String service1() throws InterruptedException, IOException {
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br><a href='/'>Home</a>[service1 sleep " + sleep +" ms]";
    }

    @RequestMapping("service2")
    public String service2() throws InterruptedException, IOException {
        String resp = httpGet("http://localhost:9091/service1");
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br><a href='/'>Home</a>[service2 sleep " + sleep +" ms]" + resp;
    }

    @RequestMapping("service3")
    public String service3() throws InterruptedException, IOException {
        String resp = httpGet("http://localhost:9092/service2");
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br><a href='/'>Home</a>[service3 sleep " + sleep +" ms]" + resp;
    }

    @RequestMapping("service4")
    public String service4() throws InterruptedException, IOException {
        String resp = httpGet("http://localhost:9093/service3");
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br><a href='/'>Home</a>[service4 sleep " + sleep+" ms]" + resp;
    }

    @RequestMapping("service5")
    public String service5() throws InterruptedException, IOException {
        String resp = httpGet("http://localhost:9093/service3");
        resp += httpGet("http://localhost:9093/service2");
        int sleep= random.nextInt(100);
        TimeUnit.MILLISECONDS.sleep(sleep);
        return "<br><a href='/'>Home</a>[service5 sleep " + sleep+" ms]" + resp;
    }

    public String httpGet(String url) throws InterruptedException, IOException {
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
