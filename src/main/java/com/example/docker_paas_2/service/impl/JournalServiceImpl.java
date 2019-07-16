package com.example.docker_paas_2.service.impl;

import com.example.docker_paas_2.dao.JournalDao;
import com.example.docker_paas_2.entry.Journal;
import com.example.docker_paas_2.service.JournalService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;


@Service
public class JournalServiceImpl implements JournalService {
    @Resource
    JournalDao journalDao;

    //用户查看日志
    public List<Journal> lookover_journal(String user_name) {
        List<Journal> journalList = journalDao.getJournalByUsername(user_name);
        Collections.reverse(journalList);
        return journalList;
    }

    //管理员查看日志
    public List<Journal> lookover_journal_all() {
        List<Journal> journalList = journalDao.getAllJournal();
        Collections.reverse(journalList);
        return journalList;
    }

}
