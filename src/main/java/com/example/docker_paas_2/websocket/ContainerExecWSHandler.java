package com.example.docker_paas_2.websocket;

import com.alibaba.fastjson.JSONObject;
import com.example.docker_paas_2.entry.ExecSession;
import com.example.docker_paas_2.service.DockerService;
import com.github.dockerjava.api.DockerClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

//连接容器执行命令
@Component
public class ContainerExecWSHandler extends TextWebSocketHandler {
    private Map<String, ExecSession> execSessionMap = new HashMap<>();

    @Resource
    DockerService dockerService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //获得传参
        String ip = session.getAttributes().get("ip").toString();
        String containerId = session.getAttributes().get("containerId").toString();
        String width = session.getAttributes().get("width").toString();
        String height = session.getAttributes().get("height").toString();

        // 连接dockerClient
        DockerClient dockerClient = dockerService.connectDocker(ip);
        //创建bash
        String execId = dockerService.create_exec_bash(dockerClient,containerId);
        //连接bash
        Socket socket = connectExec(ip, execId);
        //获得输出
        getExecMessage(session, ip, containerId, socket);
        //修改tty大小
//        resizeTty(ip, width, height, execId);
    }


    /**
     * 连接bash.
     * @param ip 宿主机ip地址
     * @param execId 命令id
     * @return 连接的socket
     * @throws IOException
     */
    private Socket connectExec(String ip, String execId) throws IOException {
        Socket socket=new Socket(ip,2375);
        socket.setKeepAlive(true);
        OutputStream out = socket.getOutputStream();
        //post web
        StringBuffer pw = new StringBuffer();
        pw.append("POST /exec/"+execId+"/start HTTP/1.1\r\n");
        pw.append("Host: "+ip+":2375\r\n");
        pw.append("User-Agent: Docker-Client\r\n");
        pw.append("Content-Type: application/json\r\n");
        pw.append("Connection: Upgrade\r\n");
        //json obj
        JSONObject obj = new JSONObject();
        obj.put("Detach",false);
        obj.put("Tty",true);
        String json = obj.toJSONString();

        pw.append("Content-Length: " + json.length() + "\r\n");
        pw.append("Upgrade: tcp\r\n");
        pw.append("\r\n");
        pw.append(json);
        //out 去写
        out.write(pw.toString().getBytes("UTF-8"));
        out.flush();
        return socket;
    }

    /**
     * 获得输出.
     * @param session websocket session
     * @param ip 宿主机ip地址
     * @param containerId 容器id
     * @param socket 命令连接socket
     * @throws IOException
     */
    private void getExecMessage(WebSocketSession session, String ip, String containerId, Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        StringBuffer returnMsg = new StringBuffer();

        while(true){
            int n = inputStream.read(bytes);
            String msg = new String(bytes,0,n);
            returnMsg.append(msg);
            bytes = new byte[10240];
            if(returnMsg.indexOf("\r\n\r\n") != -1){
                //这里好像是在发送给前端 websocket ，session是啥？？？
                session.sendMessage(new TextMessage(returnMsg.substring(returnMsg.indexOf("\r\n\r\n")+4 , returnMsg.length())));
                break;
            }
        }
        //自己做的线程 输出线程.
        // * <p>使用线程输出，方式流等待卡住</p>
        OutPutThread outPutThread = new OutPutThread(inputStream,session);
        outPutThread.start();

        //将execsion存入
        execSessionMap.put(containerId,new ExecSession(ip,containerId,socket,outPutThread));
    }

    /**
     * 修改tty大小.
     * @param ip
     * @param width
     * @param height
     * @param execId
     * @throws Exception
     */
//    private void resizeTty(String ip, String width, String height, String execId) throws Exception {
//        DockerHelper.execute(ip, docker->{
//            docker.execResizeTty(execId, Integer.parseInt(height), Integer.parseInt(width));
//        });
//    }

    /**
     * websocket关闭后关闭线程.
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String containerId = session.getAttributes().get("containerId").toString();
        ExecSession execSession = execSessionMap.get(containerId);
        if(execSession != null){
            execSession.getOutPutThread().interrupt();
        }
    }

    /**
     * 获得先输入.
     * @param session
     * @param message 输入信息
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String containerId = session.getAttributes().get("containerId").toString();
        ExecSession execSession = execSessionMap.get(containerId);
        OutputStream out = execSession.getSocket().getOutputStream();
        out.write(message.asBytes());
        out.flush();
    }
}
