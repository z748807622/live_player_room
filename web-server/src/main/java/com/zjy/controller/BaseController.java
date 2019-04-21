package com.zjy.controller;

import com.zjy.base.AjaxResult;
import com.zjy.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    protected CommonService service;

    public AjaxResult dealException(){
        return AjaxResult.error(666,"未登录");
    }

}
