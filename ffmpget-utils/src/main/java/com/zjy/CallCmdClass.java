package com.zjy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class CallCmdClass  {

    private static List<Process> pList = new ArrayList<>();
    private Object pLock = new Object();

    public static Process p;
    public static Logger logger = LoggerFactory.getLogger(CallCmdClass.class);

    /**
     * 发送cmd的错误信息
     * @param error
     */
    protected abstract void dealErrorInfo(String error);

    /**
     * 发送cmd的日志信息
     * @param log
     */
    protected abstract void dealLogInfo(String log);

    /**
     * 关闭cmd
     */
    public boolean close(){
        while (p == null);
        while (p != null){
            try {
                p.destroy();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 调用cmd命令
     * @param cmd
     */
    public boolean callCmd(String cmd){
        try {
            //Process p = Runtime.getRuntime().exec("ping 172.247.34.70");
            //String cmd = "ffmpeg -stream_loop -1 -re -i D:/video/哈利波特第8部哈利波特与死亡圣器(下).mp4 -f mpegts -codec:v mpeg1video -s 864*486 udp://127.0.0.1:4444";
            //String cmd2 = "ffmpeg";
            //String cmd3 = "ping 172.247.34.70";
            logger.info("执行：{}",cmd);
            p = Runtime.getRuntime().exec(cmd);
            synchronized (pLock){
                pList.add(p);
            }
            logger.info("开始.. : {}",cmd);

            new Thread(()->{
                try {
                    InputStream is = p.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //System.out.println("info--"+line);
                        dealLogInfo(line);
                    }
                    is.close();
                    reader.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    InputStream is = p.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //System.out.println("error -- "+line);
                        dealErrorInfo(line);
                    }
                    is.close();
                    reader.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
            p.waitFor();
            Thread.sleep(1000);
            p.destroy();
            logger.info("停止:{}",cmd);
        }catch (Exception e){
            e.printStackTrace();
            logger.info("异常：{}",cmd);
            if(p != null){
                p.destroy();
            }
            return false;
        }
        return true;
    }
}
