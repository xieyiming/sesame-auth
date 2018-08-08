# 芝麻信用授权
## 背景介绍
芝麻信用分是，阿里推出的一款征信产品，在金融风控方面可以提供，参考和应用，阿里官方也给出了相应的开放平台，可以申请用户授权，查询。这里是官方接入文档[页面授权](https://b.zmxy.com.cn/technology/openDoc.htm?relInfo=zhima.auth.info.authorize@1.0@1.3)，这个是H5版接入文档，通过这种方式接入可以跨平台应用在各个客户端。

## 注册流程
想要接入芝麻信用的公司，需要在芝麻信用平台注册申请，商家入驻，注册申请成功后然后在产品商店选择，芝麻分

![avatar](http://chuantu.biz/t6/352/1533288887x-1566688712.png)

签约成功后，就可以开通服务，在注册增加一个应用，在这里设置回调地址和，公钥，私钥

![avatar](http://chuantu.biz/t6/352/1533290795x1822611413.png)

## 开发流程
需要使用官方的SDK，这里需要把这个SDK，导入到MVN仓库中，我这里导入的是本地仓库

``` 
mvn install:install-file -Dfile=D:\芝麻信用\SDK\D:\芝麻信用\SDK\zmxy-sdk-java-20170320112636.jar -DgroupId=com.github.xieyiming -DartifactId=sesame-sdk -Dversion=1.0 -Dpackaging=jar
```


具体开发，我们可以根据泳道图来进行业务逻辑梳理
### 泳道图
![avatar](http://chuantu.biz/t6/353/1533522473x-1566688718.png)

通过泳道图我们可以直观的看到，我们只需要开发两个接口:

一个用户请求授权接口，这里需要的参数为，用户的个人信息，身份证和姓名还有，授权完成后需要重定向的地址，将客户的。


另一个是，用户在芝麻信用端，授权完成后，芝麻信用通知我们授权结果的回调地址。如果用户授权成功，芝麻会将客户的openid返回，通过这个openid来查询用户的芝麻信用分

用时序图表示，可以清晰的展示出消息序列，传递消息的时间顺序，描述消息是如何在前端和芝麻信用间发送和接收的逻辑。
### 时序图
![avatar](http://chuantu.biz/t6/353/1533534842x-1566688712.png)

## 请求示例
POST：/auth/loginInfo/v1
{
    "idCard": "430802199411110317",
    "name":"谢一鸣",
}

返回重定向地址，由浏览器自动重定向到芝麻信用的授权页面：
redirect:https://zmopenapi.zmxy.com.cn/openapi.do?charset=UTF-8&method=zhima.auth.info.authorize&channel=app&sign=RBnhz%2BqS%2B0P07yL2ozVQC0eSA8ArdFwix3HuZult5%2Bamzy5KWYMl90BNQrX4MPuPntOquvUEurDMNKVhKZlE1fUoSIyiappqPoToXBwGKefe0i07PZb3OPY0Ff4SdEQpKniJgP0d5WL%2BO6W%2BziL8QFTqBDXAmlo0AMeHUCUgr7s%3D&version=1.0&app_id=1002208&sign_type=RSA&platform=zmop&params=Iz3EX7wZGg3IY0NCDGwAHJTOZ5US2thMKBgisxKIvc4Ecn7FaZR3L1h9G2Xb9K4DrTaTYFilpe%2BN4E5rAs7K5a%2FfLx2eGkqIxcMLTUPuJ%2BLi4YvIEVVQxfU%2F1WhxMgDGaDsNVnhV06O1pJxty5MsHvN1C3KEZ6VWBaoIb33H2aM56DSE%2BVEOXTGoKGYBbU%2B9JXqjJzleT%2FvxZYRFNhUIEICgspSe2PrRE1QHPyDgZnZfWY7mnCbvwfwL5inT%2F6rWX9gJwl9bVmyHlsyhsIwCMP6%2BRRkeSO85sSWPTFmKOeJCXuE36Shrb7JIkXgNA%2F%2BicRcQbsGokrlLIxLwE7mDQzjOONXSOiYOOKAS%2FE24Zdua9A2rkYYnYiu2ZPK5dUbSSLgihCjeyL1cf3e7Za2WnmZOBRqjYj5KZpoLkjcymmDDnmIWgagKvbAe14%2FC9k5dZvAVrSj07TO8DULiPHA95%2B6LtfFTCr8RCnqdYQHM5K8tkzpFOmYnjJouen7Pu7r3

