package com.example.docker_paas_2.controller;


import com.example.docker_paas_2.entry.Journal;
import com.example.docker_paas_2.service.JournalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@CrossOrigin
@ResponseBody
public class JournalController {
    @Resource
    JournalService journalService;

    private static final Logger LOG = LoggerFactory.getLogger(SwarmController.class);

    //用户查看日志
    @RequestMapping(value = "/request_lookover_journal")
    public List<Journal> lookover_journal(@RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("用户查看日志=========");
        System.out.println("user_name:" + user_name);
        LOG.info("===用户查看日志=== " + "user_name:" + user_name);

        //调用service
        return journalService.lookover_journal(user_name);
    }

    //管理员查看日志
    @RequestMapping(value = "/request_lookover_journal_all")
    public List<Journal> lookover_journal_all() throws Exception{

        System.out.println("管理员查看日志=========");
        LOG.info("===管理员查看日志=== ");

        //调用service
        return journalService.lookover_journal_all();
    }


}
