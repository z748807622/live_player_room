package com.zjy.controller;

import com.zjy.base.AjaxResult;
import com.zjy.entity.VideoFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ApiController extends BaseController {

    @Value("${upload.filePath}")
    private String filePath;

    @RequestMapping(value = "/video/{videoId}", method = RequestMethod.DELETE)
    public AjaxResult delVideoFile(@PathVariable("videoId") Integer videoId){
        this.service.getVideoFileMapper().deleteByPrimaryKey(videoId);
        return AjaxResult.success("删除成功");
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file){
        if (file != null){
            VideoFile videoFile = new VideoFile();
            String fileName = file.getOriginalFilename();
            String path = filePath + "/" + UUID.randomUUID() + fileName;
            File file1 = new File(path);
            try {
                file.transferTo(file1);
            } catch (IOException e) {
                e.printStackTrace();
                return AjaxResult.error("上传错误");
            }
            videoFile.setVideoName(fileName);
            videoFile.setVideoPath(path);
            this.service.getVideoFileMapper().insert(videoFile);
            return AjaxResult.success("上传成功");
        }
        return AjaxResult.success("上传失败");
    }


    @RequestMapping(value = "/video",method = RequestMethod.GET)
    public AjaxResult getFileList(){
        return AjaxResult.success("查询成功").put("data",this.service.getVideoFileMapper().selectAll());
    }

    public AjaxResult

}
