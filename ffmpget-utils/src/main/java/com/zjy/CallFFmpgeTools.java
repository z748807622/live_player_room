package com.zjy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CallFFmpgeTools extends CallCmdClass {

    Logger logger = LoggerFactory.getLogger(CallFFmpgeTools.class);
    //drawtext="fontfile=arial.ttf:x=w-tw:fontcolor=white:fontsize=30:text='%{localtime\:%H\:%M\:%S}'"   时间戳
    //ffmpeg  -re -i D:/video/有头脑的祖先.mp4 -vcodec libx264 -f mpegts -codec:v mpeg1video udp://127.0.0.1:2222
    private String ffmpge = "ffmpeg {loop} -re -i {file} -vcodec libx264 -f mpegts -codec:v mpeg1video udp://127.0.0.1:8888";

    /**
     * 设置播放文件名和是否循环播放
     * @param fileName
     * @param isLoop
     * @return
     */
    public void startPlay(String fileName, boolean isLoop){
        if (isLoop){
            ffmpge = ffmpge.replace("{loop}","-stream_loop -1");
        }else {
            ffmpge = ffmpge.replace("{loop}","");
        }
        if(!StringUtils.isBlank(fileName)){
            ffmpge = ffmpge.replace("{file}",fileName);
        }
        logger.info("cmd执行：{}",ffmpge);
        super.callCmd(ffmpge);
    }

}
