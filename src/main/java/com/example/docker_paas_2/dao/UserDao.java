package com.example.docker_paas_2.dao;


import com.example.docker_paas_2.entry.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface UserDao {

    //根据用户名从数据库里读取一条用户记录,----->用于注册，查找用户
    public User getByUserName(String user_name);

    //根据邮箱从数据库里读取一条用户记录
    public User getByUserEmail(String email);

    //数据库插入一条用户信息
    public int insertUser(String user_name, String password, String email);

    //根据power查看用户！！！查看
    public ArrayList<User> getUserByPowerAll();
    public ArrayList<User> getUserByPower(String power);

    //删除用户
    public int deleteByUserName(String user_name);

    //修改用户权限
    public int updatePower(String user_name, String power);

    //修改验证码
    public int updateVerifyCode(String user_name, String verify_code);

    //重置密码
    public int updatePassword(String user_name, String new_password);
}
