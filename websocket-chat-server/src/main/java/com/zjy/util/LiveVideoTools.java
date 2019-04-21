package com.zjy.util;

import com.alibaba.fastjson.JSON;
import com.zjy.CallFFmpgeTools;
import com.zjy.handler.UserInfoManager;
import com.zjy.proto.ChatCode;

import java.util.HashMap;
import java.util.Map;

public class LiveVideoTools extends CallFFmpgeTools {


    @Override
    protected void dealErrorInfo(String error) {
        this.sendLiveMess(0,error);
    }

    @Override
    protected void dealLogInfo(String log) {
        this.sendLiveMess(1,log);
    }

    /**
     *
     * @param type 0为错误信息 1为日志信息
     * @param mess 信息
     */
    private void sendLiveMess(int type, String mess){
        Map<String,String> res = new HashMap<>();
        res.put("code",String.valueOf(ChatCode.SYS_ONLINE_MESSAGE));
        res.put("type",String.valueOf(type));
        res.put("mess",mess);
        System.out.println(JSON.toJSONString(res));
        UserInfoManager.sendLiveCmdInfo(JSON.toJSONString(res));
        //System.out.println(JSON.toJSONString(res));
    }
}
