package com.github.xieyiming.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.xieyiming.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;


/** 
* @author 作者 xym: 
* @version 创建时间：2018年7月20日 上午11:15:21 
* 类说明 
*/
@Controller
@RequestMapping( value = "/auth" )
public class AuthController {

	@Autowired
	AuthService authService;

	
	@RequestMapping(value =  "/loginInfo/v1" ,produces = "application/json;charset=utf-8")
	public String loginInfo(@RequestBody String jsonStr,HttpServletRequest request) throws Exception {
		JSONObject params = JSONObject.parseObject(jsonStr);
		JSONObject returnValue = authService.checkLoginInfo(params);

		//芝麻信用的授权地址
		String returnUrl = returnValue.getString("data");

		return "redirect:"+returnUrl;
	}

	
	@RequestMapping(value="/callback", method = RequestMethod.GET)
	public String callback(@RequestParam("params") String params, @RequestParam("sign") String sign, HttpServletResponse response) throws Exception {
		JSONObject jsonStr = new JSONObject();
		jsonStr.put("params", params.trim());
		jsonStr.put("sign", sign.trim());
		JSONObject returnValue = authService.getCallbackInfo(jsonStr);

		
		//获取回调地址
		String returnUrl = returnValue.getString("returnUrl");
		if(StringUtils.isNotBlank(returnUrl)){
			if(returnUrl.contains("http")){
				returnUrl =  "redirect:"+returnUrl;
			}
			returnUrl = "redirect:http://"+returnUrl;
		}
		
		return returnUrl;
		

	}
	
	
	
}
