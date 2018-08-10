package com.github.xieyiming.service;


import java.text.SimpleDateFormat;
import java.util.Date;


import com.antgroup.zmxy.openplatform.api.ZhimaApiException;
import com.github.xieyiming.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.antgroup.zmxy.openplatform.api.DefaultZhimaClient;
import com.antgroup.zmxy.openplatform.api.request.ZhimaAuthInfoAuthorizeRequest;


/**
 *  
 *
 * @author 作者 xym: 
 * @version 创建时间：2018年7月20日 上午11:54:31 
 * 类说明 
 */
@Service
public class AuthService {


    @Autowired
    MongoTemplate mongoTemplate;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Logger logger = LoggerFactory
            .getLogger(AuthService.class);

    @Value("${sesame.privatekey}")
    private String privateKey;
    @Value("${sesame.publickey}")
    private String publicKey;
    @Value("${sesame.appid}")
    private String appId;
    @Value("${sesame.url}")
    private String gatewayUrl;


    //第一步芝麻信用平台校验
    public JSONObject checkLoginInfo(JSONObject params) throws ZhimaApiException {
        logger.info("芝麻信用设置访问地址开始:" + params.toJSONString());
        JSONObject returnValue = new JSONObject();

        JSONObject identityParam = new JSONObject();
        JSONObject bizParams = new JSONObject();
        identityParam.put("name", params.getString("name").trim());
        identityParam.put("certType", "IDENTITY_CARD");
        identityParam.put("certNo", params.getString("id_card").trim());
        bizParams.put("auth_code", "M_H5");
        bizParams.put("channelType", "app");
        bizParams.put("state", params.getString("id_card").trim());

        ZhimaAuthInfoAuthorizeRequest req = new ZhimaAuthInfoAuthorizeRequest();
        req.setChannel("app");
        req.setPlatform("zmop");
        req.setIdentityType("2");// 必要参数
        req.setIdentityParam(identityParam.toJSONString());// 必要参数
        req.setBizParams(bizParams.toJSONString());//
        DefaultZhimaClient client = new DefaultZhimaClient(gatewayUrl, appId, privateKey,
                publicKey);
        String url = client.generatePageRedirectInvokeUrl(req);
        returnValue.put("data", url);
        returnValue.put("name", params.getString("name").trim());
        returnValue.put("idCard", params.getString("idCard").trim());
        returnValue.put("_id", params.getString("idCard").trim());
        returnValue.put("returnUrl", params.getString("returnUrl"));
        returnValue.put("update_time", new Date());
        logger.info("芝麻信用设置访问地址结束:" + returnValue.toJSONString());
        mongoTemplate.save(returnValue, Constant.AUTH_LOGININFO_COLLECTION);
        return returnValue;

    }

    //第二步芝麻信用回调，通知客户授权成功还是授权失败
    public JSONObject getCallbackInfo(JSONObject params) throws ZhimaApiException {
        logger.info("芝麻信用回调开始:" + params.toJSONString());

        //解析回调信息
        DefaultZhimaClient client = new DefaultZhimaClient("http://127.0.0.1/auth/callback", appId, privateKey, publicKey);
        JSONObject returnValue = new JSONObject();

        String result = client.decryptAndVerifySign(params.getString("params").trim(), params.getString("sign").trim());
        String[] strs = result.split("&");
        String idcard = "";
        for (String str : strs) {
            if (str.contains("state")) {
                idcard = str.substring(6, str.length());
                break;
            }
        }
        returnValue.put("_id", idcard.trim());
        returnValue.put("data", result);
        returnValue.put("update_time", new Date());

        //保存回调信息
        mongoTemplate.save(returnValue, Constant.AUTH_CALLBACKINFO_COLLECTION);

        //进一步解析回调信息，并更新授权结果
        returnValue = this.getResultAuthInfo(returnValue);

        logger.info("芝麻信用回调结束:" + params.toJSONString());
        return returnValue;

    }


    private JSONObject getResultAuthInfo(JSONObject params) {

        JSONObject returnValue = new JSONObject();
        Update update = new Update();
        String data = params.getString("data");
        String _id = params.getString("_id");
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(_id));

        //查询注册结果获取用户回调地址
        JSONObject result = mongoTemplate.findOne(query, JSONObject.class, Constant.AUTH_LOGININFO_COLLECTION);
        if (result.containsKey("returnUrl")) {
            returnValue.put("returnUrl", result.getString("returnUrl"));
        }

        //解析回调的授权结果
        String strs[] = data.split("&");
        String success_result = "";
        String open_id = "";
        for (String str : strs) {
            if (str.contains("success")) {
                success_result = str.substring(8, str.length());
            }
            if (str.contains("open_id")) {
                open_id = str.substring(8, str.length());
                result.put("open_id", open_id);
            }
        }

        if (success_result.equals("true")) {
            update.set("code", "200");
            update.set("open_id", open_id);
            update.set("msg", "授权成功");
        } else {

            update.set("code", "0001");
            update.set("msg", "授权失败");
        }

        //更新授权结果表
        mongoTemplate.updateFirst(query, update, JSONObject.class, Constant.AUTH_LOGININFO_COLLECTION);
        return returnValue;

    }


}
