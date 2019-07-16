package com.example.docker_paas_2.controller;

import com.example.docker_paas_2.entry.User;
import com.example.docker_paas_2.service.UserService;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;

@RestController
@CrossOrigin
@ResponseBody
public class UserController {
    @Resource
    UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(SwarmController.class);

    //用户登录
    @RequestMapping(value = "/request_user_login")
    public String login(@RequestParam(value = "user_name") String user_name,
                        @RequestParam(value = "password") String password) throws Exception{

        System.out.println("用户登录=========");
        System.out.println("user_name:" + user_name);
        System.out.println("password:" + password);
        LOG.info("===用户登录=== " + "user_name:" + user_name + ",password:" + password);

        //调用service
        return userService.check_user_login(user_name, password);
    }

    //用户注册
    @RequestMapping(value = "/request_user_register")
    public String register(@RequestParam(value = "user_name") String user_name,
                        @RequestParam(value = "password") String password,
                           @RequestParam(value = "email") String email) throws Exception{

        System.out.println("用户注册=========");
        System.out.println("user_name:" + user_name);
        System.out.println("password:" + password);
        System.out.println("email:" + email);
        LOG.info("===用户注册=== " + "user_name:" + user_name + ",password:" + password + ",email:" + email);

        //调用service
        return userService.user_register(user_name, password, email);
    }

    //管理员查找用户信息
    @RequestMapping(value = "/request_user_search_by_id")
    public ArrayList<User> find_user(@RequestParam(value = "user_name") String user_name,
                                     @RequestParam(value = "admin_name") String admin_name) throws Exception{

        System.out.println("管理员查找用户信息=========");
        System.out.println("user_name:" + user_name);
        System.out.println("admin_name:" + admin_name);
        LOG.info("===管理员查找用户信息=== " + "user_name:" + user_name + ",admin_name:" + admin_name);

        //调用service
        return userService.user_find(user_name, admin_name);
    }

    //根据power查看用户
    @RequestMapping(value = "/request_user_search_by_power")
    public ArrayList<User> see_user(@RequestParam(value = "power") String power,
                                    @RequestParam(value = "admin_name") String admin_name) throws Exception{

        System.out.println("管理员根据权限查看用户=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("power:" + power);
        LOG.info("===管理员根据权限查看用户=== " + "power:" + power);

        //调用service
        return userService.user_check(power, admin_name);
    }

    //请求删除用户
    @RequestMapping(value = "/request_user_delete")
    public String delete_user(@RequestParam(value = "user_name") String user_name,
                              @RequestParam(value = "admin_name") String admin_name) throws Exception{

        System.out.println("管理员删除用户=========");
        System.out.println("user_name:" + user_name);
        LOG.info("===管理员删除用户=== " + "user_name:" + user_name);

        //调用service
        return userService.user_delete(user_name, admin_name);
    }

    //请求修改用户权限
    @RequestMapping(value = "/request_user_power_update")
    public String user_power_modify(@RequestParam(value = "user_name") String user_name,
                                    @RequestParam(value = "power") String power,
                                    @RequestParam(value = "admin_name") String admin_name) throws Exception{

        System.out.println("管理员修改用户权限=========");
        System.out.println("user_name:" + user_name);
        System.out.println("power:" + power);
        LOG.info("===管理员修改用户权限=== " + "user_name:" + user_name + ",power:" + power);

        //调用service
        return userService.user_power_update(user_name, power, admin_name);
    }

    //请求发送验证码
    @RequestMapping(value = "/request_send_verify_code")
    public String send_verify_code(@RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("请求发送验证码=========");
        System.out.println("user_name:" + user_name);
        LOG.info("===请求发送验证码=== " + "user_name:" + user_name);

        //调用service
        return userService.send_verify_code(user_name);
    }


    //请求重置密码
    @RequestMapping(value = "/request_user_reset_password")
    public String user_reset_password(@RequestParam(value = "user_name") String user_name,
                                      @RequestParam(value = "verify_code") String verify_code,
                                      @RequestParam(value = "new_password") String new_password) throws Exception{

        System.out.println("请求重置密码=========");
        System.out.println("user_name:" + user_name);
        System.out.println("verify_code:" + verify_code);
        System.out.println("new_password:" + new_password);
        LOG.info("===请求重置密码=== " + "user_name:" + user_name + ",verify_code:" + verify_code + ",new_password:" + new_password);

        //调用service
        return userService.user_reset_password(user_name, verify_code, new_password);
    }

}
