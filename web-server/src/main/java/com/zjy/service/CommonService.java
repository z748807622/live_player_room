package com.zjy.service;

import com.zjy.entity.SysAdmin;
import com.zjy.mapper.PermissionMapper;
import com.zjy.mapper.SysAdminMapper;
import com.zjy.mapper.SysUserMapper;
import com.zjy.mapper.VideoFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysAdminMapper adminMapper;
    @Autowired
    private VideoFileMapper videoFileMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    public SysUserMapper getUserMapper() {
        return userMapper;
    }

    public void setUserMapper(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SysAdminMapper getAdminMapper() {
        return adminMapper;
    }

    public void setAdminMapper(SysAdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public VideoFileMapper getVideoFileMapper() {
        return videoFileMapper;
    }

    public void setVideoFileMapper(VideoFileMapper videoFileMapper) {
        this.videoFileMapper = videoFileMapper;
    }

    public PermissionMapper getPermissionMapper() {
        return permissionMapper;
    }

    public void setPermissionMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
}
