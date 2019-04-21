package com.zjy.util;

public enum FFMPEG {

    TOOLS;

    private LiveVideoTools tools = null;

    private FFMPEG(){
        tools = new LiveVideoTools();
    }

    public LiveVideoTools getTools(){
        return tools;
    }

}
