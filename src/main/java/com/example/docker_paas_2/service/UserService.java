package com.example.docker_paas_2.service;

import com.example.docker_paas_2.entry.User;

import java.util.ArrayList;


public interface UserService {

    //检查用户登录信息是否正确
    public String check_user_login(String user_name, String password);

    //注册
    public String user_register(String user_name, String password, String email);

    //根据username查找用户！！！查找一个人
    public ArrayList<User> user_find(String user_name, String admin_name);

    //根据power查看用户！！！查看一群人
    public ArrayList<User> user_check(String user_name, String admin_name);

    //删除用户,root不可以删除
    public String user_delete(String user_name, String admin_name);

    //修改用户权限,root不可以修改，而且也不可以将权限修改成root,也不可以修改权限为其他的随便字符
    public String user_power_update(String user_name, String power, String admin_name);

    //请求发送验证码
    public String send_verify_code(String user_name);

    //请求重置密码
    public String user_reset_password(String user_name, String verify_code, String new_password);

}
