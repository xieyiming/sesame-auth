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
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.antgroup.zmxy.openplatform.api.DefaultZhimaClient;
import com.antgroup.zmxy.openplatform.api.request.ZhimaCreditScoreGetRequest;
import com.antgroup.zmxy.openplatform.api.response.ZhimaCreditScoreGetResponse;


/** 
* @author 作者 xym: 
* @version 创建时间：2018年4月31日 上午11:04:14 
* 类说明 
*/
@Service
public class ScoreService {

	@Value("${sesame.privatekey}")
	private String privateKey;
	@Value("${sesame.publickey}")
	private String publicKey;
	@Value("${sesame.appid}")
	private String appId;
	@Value("${sesame.url}")
	private String gatewayUrl;

	@Autowired
	MongoTemplate mongoTemplate;

	
	private static Logger logger = LoggerFactory
			.getLogger(ScoreService.class);

	

	private JSONObject getCacheDataFromDB(JSONObject data) throws Exception {
		JSONObject returnValue = null;

		Query query = new Query();;
		String _id = data.getString("idCard").trim();
		query.addCriteria(new Criteria("_id").is(_id));
		JSONObject cache  =mongoTemplate.findOne(query, JSONObject.class,
				Constant.AUTH_LOGININFO_COLLECTION);
		if (cache != null && cache.containsKey("zm_score")) {
			// 判断更新时间
			long time = cache.getDate("update_time").getTime();
			long date = new Date().getTime();
			long diff = date -time;// 这样得到的差值是微秒级别
			long days = diff / (1000 * 60 * 60 * 24);
			if (days > Constant.DEFAULT_CACHETIME) {
				returnValue =  null;
			}else {
				returnValue = JSONObject.parseObject(cache.toString());
			}

		}

		return returnValue;

	}

	
	
	//查询芝麻信用分
	public JSONObject getServiceInfo(JSONObject params) throws Exception {
		JSONObject returnValue = this.getCacheDataFromDB(params);

		if(returnValue==null){
			//获取open_id
			String open_id = this.getOpenId(params);
			JSONObject scoreResult = null;


			if(!open_id.isEmpty()){
				scoreResult = this.getCreditScore(open_id, params);
				//成功获取芝麻信用分
				if(scoreResult.getString("success").equals("true")){
					returnValue = JSONObject.parseObject(params.toString());
					returnValue.put("_id", params.getString("idCard").trim());
					returnValue.put("zm_score", scoreResult.getString("zm_score").trim());
					returnValue.put("update_time", new Date());
					mongoTemplate.save(returnValue,Constant.QUERY_SCORE_COLLECTION);
				//客户可能取消授权
				}else {
					returnValue.put("code", "002");
					returnValue.put("msg", "未授权");
				}

			}else {
				returnValue.put("code", "002");
				returnValue.put("msg", "未授权");
			}

		}

		
		return returnValue;
	}
	
	private JSONObject getCreditScore(String open_id,JSONObject params) throws ZhimaApiException {
		logger.info("芝麻信用分调用开始:"+params.toJSONString());
		long startTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS"); 
		String  transactionId = sdf.format(new Date())+String.valueOf(startTime).substring(0, 12);
		JSONObject returnValue = new JSONObject();
		
		ZhimaCreditScoreGetRequest req = new ZhimaCreditScoreGetRequest();
        req.setPlatform("zmop");
        req.setTransactionId(transactionId);// 必要参数         
        //芝麻信用分产品ID
        req.setProductCode("w1010100100000000001");// 必要参数         
        req.setOpenId(open_id);// 必要参数         
        DefaultZhimaClient client = new DefaultZhimaClient(gatewayUrl, appId, privateKey,
				publicKey);

            ZhimaCreditScoreGetResponse response =(ZhimaCreditScoreGetResponse)client.execute(req);
            returnValue.put("biz_no", response.getBizNo());
            returnValue.put("zm_score", response.getZmScore());
            returnValue.put("errorcode", response.getErrorCode());
            returnValue.put("errormessage", response.getErrorMessage());
            returnValue.put("success", response.isSuccess());
            

        JSONObject clone = new JSONObject();
		clone = JSONObject.parseObject(params.toString());
		clone.put("_id", params.getString("idCard").trim());
		clone.put("data", returnValue);
		clone.put("update_time", new Date());
		Long timeUsed = System.currentTimeMillis()-startTime;
		logger.info("芝麻信用分调用结束:"+params.toJSONString());
		//上游返回记录原始库
		mongoTemplate.save(clone,Constant.SCORE_RESULT_COLLECTION);
        
		return returnValue;
		
	}
	
	//查询是否授权成功拥有芝麻open_id
	private String getOpenId(JSONObject params){
		JSONObject loginInfo = null;
		String open_id = "";
		String idCard = params.getString("idCard").trim();
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(idCard));


		loginInfo = mongoTemplate.findOne(query, JSONObject.class,
					Constant.AUTH_LOGININFO_COLLECTION);
		
		if(loginInfo!=null&&loginInfo.containsKey("code")&&loginInfo.getString("code").equals("200")){
			open_id = loginInfo.getString("open_id").trim();
		}
		
		return open_id;
		
	}

}
