package com.example.docker_paas_2.service.impl;

import com.example.docker_paas_2.dao.JournalDao;
import com.example.docker_paas_2.dao.SwarmDao;
import com.example.docker_paas_2.dao.UserDao;
import com.example.docker_paas_2.entry.Swarm;
import com.example.docker_paas_2.entry.User;
import com.example.docker_paas_2.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserDao userDao;
    @Resource
    SwarmDao swarmDao;
    @Resource
    JournalDao journalDao;
    @Resource
    JavaMailSender mailSender;

    @Value("${mail.fromMail.addr}")
    private String from;

    //检查用户登录信息是否正确
    public String check_user_login(String user_name, String password){

        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, user_name, "登录");

        //从数据库里读取一条用户信息
        User user = userDao.getByUserName(user_name);
        if(user == null) {
            return "user_not_exist";
        } else if(password.equals(user.getPassword())) {
            return user.getPower();
        } else {
            return "wrong_password";
        }
    }

    //注册
    public String user_register(String user_name, String password, String email) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);


        //判断邮箱是否合法
        if(!isEmail(email)) {
            journalDao.insertJournal(nowTime, user_name, "注册账号失败-->邮箱不合法");
            return "email_invalid";
        }

        //判断用户名是否已经存在
        User user = userDao.getByUserName(user_name);
        int ret = 0; //数据库插入数据的返回值

        if(user == null) {
            //判断邮箱是否已存在
            User user_email = userDao.getByUserEmail(email);
            if(user_email == null) {
                //数据库插入用户信息
                ret = userDao.insertUser(user_name, password, email);
            } else {
                //邮箱已存在
                journalDao.insertJournal(nowTime, user_name, "注册账号失败-->邮箱已存在");
                return "email_exist";
            }
        } else {
            journalDao.insertJournal(nowTime, user_name, "注册账号失败-->用户名已存在");
            return "user_name_exist";
        }

        if(ret > 0) {
            journalDao.insertJournal(nowTime, user_name, "注册账号成功");
            sendEmail(user_name, "register", ""); //发送邮件
            return "success";
        } else {
            journalDao.insertJournal(nowTime, user_name, "注册账号失败-->录入数据库错误");
            return "null";
        }
    }

    //根据user_name查找用户！！！查找
    public ArrayList<User> user_find(String user_name, String admin_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "查找用户" + user_name + "信息");

        User user = userDao.getByUserName(user_name);
        ArrayList<User> users = new ArrayList<User>();
        users.add(user);
        return users;
    }

    //根据power查看用户！！！查看
    public ArrayList<User> user_check(String power, String admin_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "查看所有" + power + "的信息");

        if(power.equals("all")) {
            return userDao.getUserByPowerAll();
        } else if(power.equals("user") || power.equals("admin")) {
            return userDao.getUserByPower(power);
        } else {
            return null;
        }
    }

    //删除用户,root不可以删除
    public String user_delete(String user_name, String admin_name) {
        //记录日志
        Date date = new Date();
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);


        //root不可以删除
        if(user_name.equals("root")) {
            journalDao.insertJournal(nowTime, admin_name, "删除用户失败-->删除root");
            return "root_cannot_delete";
        }

        //查看swarm_service表里该用户是否有service,如果有就无法删除用户
        List<Swarm> swarms = swarmDao.getByUserName(user_name);
        if(!swarms.isEmpty()) {
            journalDao.insertJournal(nowTime, admin_name, "删除用户失败-->该用户有服务未删除");
            return "not_delete_service";
        }

        //删除数据库数据
        int ret = userDao.deleteByUserName(user_name);
        if(ret > 0) {
            journalDao.insertJournal(nowTime, admin_name, "删除用户成功，用户名：" + user_name);
            sendEmail(user_name, "delete", ""); //发送邮件
            return "success";
        } else {
            journalDao.insertJournal(nowTime, admin_name, "删除用户失败-->数据库修改失败");
            return "null";
        }
    }

    //修改用户权限,root不可以修改，而且也不可以将权限修改成root,也不可以修改权限为其他的随便字符
    public String user_power_update(String user_name, String power, String admin_name) {
        //记录日志
        Date date = new Date();
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);


        //root不可以修改，而且也不可以将权限修改成root
        if(user_name.equals("root") || power.equals("root")) {
            journalDao.insertJournal(nowTime, admin_name, "修改权限失败-->修改root");
            return "null";
        } else {
            //不可以修改权限为其他的随便字符
            if(!power.equals("user") && !power.equals("admin")) {
                journalDao.insertJournal(nowTime, admin_name, "修改权限失败-->修改位置权限");
                return "null";
            } else {
                int ret = userDao.updatePower(user_name, power);
                if (ret > 0) {
                    journalDao.insertJournal(nowTime, admin_name, "修改权限成功，用户名：" + user_name);
                    sendEmail(user_name, "power_update", power); //发送邮件
                    return "success";
                } else {
                    journalDao.insertJournal(nowTime, admin_name, "修改权限失败-->数据库修改失败");
                    return "fail";
                }
            }
        }
    }

    //请求发送验证码
    public String send_verify_code(String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, user_name, "请求发送验证码");

        Boolean flag = sendEmail(user_name, "reset", ""); //发送邮件
        if(flag == true) {
            return "success";
        } else {
            return "fail";
        }
    }

    //请求重置密码
    public String user_reset_password(String user_name, String verify_code, String new_password) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, user_name, "请求重置密码");

        //根据用户名得到邮箱地址
        User user = userDao.getByUserName(user_name);
        if(verify_code.equals(user.getVerify_code())) {
            userDao.updatePassword(user_name, new_password);
            sendEmail(user_name, "reset_success", ""); //发送邮件
            return "success";
        }
        return "error_code";
    }






    //发送邮件
    public boolean sendEmail(String user_name, String action, String power) {
        //根据用户名得到邮箱地址
        User user = userDao.getByUserName(user_name);
        String email = user.getEmail();
//        String email = "87583622@qq.com";

        //生成6位随机数
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        String subject = "docker_paas";
        String content = String.valueOf(code);

        //生成邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);

        switch (action) {
            case "register":
                message.setSubject("docker_paas注册");
                message.setText("账号注册成功!感谢使用docker_paas");
                break;
            case "delete":
                message.setSubject("docker_paas注销");
                message.setText("您的账号已被注销!");
                break;
            case "reset":
                message.setSubject("docker_paas重置密码");
                message.setText("docker_paas 验证码：" + content);
                userDao.updateVerifyCode(user_name, content);  //验证码写入数据库
                break;
            case "reset_success":
                message.setSubject("docker_paas重置密码");
                message.setText("密码重置成功!感谢使用docker_paas");
                break;
            case "power_update":
                message.setSubject("docker_paas 账户权限调整");
                message.setText("你的docker_paas账户权限已被修改，当前权限为：" + power);
                break;
        }
        try {
            mailSender.send(message);
            System.out.println("Send email success");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send email fail");
            return false;
        }
        return true;
    }

    //判断邮箱是否合法
    public static boolean isEmail(String email) {
        if (email == null)
            return false;
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(email);
        if (m.matches())
            return true;
        else
            return false;
    }


}
