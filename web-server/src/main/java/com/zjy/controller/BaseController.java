package com.zjy.controller;

import com.zjy.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    protected CommonService service;

}
