package com.example.docker_paas_2.dao;


import com.example.docker_paas_2.entry.Journal;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Time;
import java.util.ArrayList;

@Mapper
public interface JournalDao {
    //插入一条日志
    public int insertJournal(String time, String user_name, String info);

    //读取用户自己日志
    public ArrayList<Journal> getJournalByUsername(String user_name);

    //读取所有日志
    public ArrayList<Journal> getAllJournal();
}
