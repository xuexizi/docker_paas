package com.example.docker_paas_2.service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTime {

    public static void main(String[] args) {
        Date date = new Date();//获得系统时间.
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        System.out.println(nowTime);
    }

}
