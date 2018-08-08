package com.github.xieyiming.controller;


import javax.servlet.http.HttpServletRequest;

import com.github.xieyiming.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import org.springframework.web.bind.annotation.RestController;

/** 
* @author 作者 xym: 
* @version 创建时间：2018年3月31日 上午10:58:08 
* 类说明 
*/
@RestController
@RequestMapping(path={ "/score" },produces = "application/json;charset=utf-8")
public class ScoreContoller {
	
	@Autowired
	ScoreService service;

	
	@RequestMapping({ "/queryScore/v1" })
	public JSONObject queryScore(@RequestBody String jsonStr,HttpServletRequest request) throws Exception {
		JSONObject params = JSONObject.parseObject(jsonStr);
		JSONObject returnValue = service.getServiceInfo(params);

		return returnValue;
	}
}
