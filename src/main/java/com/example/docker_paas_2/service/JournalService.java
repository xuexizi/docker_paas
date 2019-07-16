package com.example.docker_paas_2.service;

import com.example.docker_paas_2.entry.Journal;
import java.util.List;

public interface JournalService {

    //用户查看日志
    public List<Journal> lookover_journal(String user_name);

    //管理员查看日志
    public List<Journal> lookover_journal_all();

}
