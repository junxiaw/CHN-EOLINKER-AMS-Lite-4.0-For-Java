package com.eolinker.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eolinker.pojo.Partner;
import com.eolinker.pojo.TestRequestParam;
import com.eolinker.service.ApiService;
import com.eolinker.service.ProjectService;
import com.eolinker.service.TestHistoryService;
import com.eolinker.util.DateUtil;
import com.eolinker.util.Proxy;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 接口测试控制器
 * @name eolinker ams open source，eolinker开源版本
 * @link https://www.eolinker.com/
 * @package eolinker
 * @author www.eolinker.com 广州银云信息科技有限公司 2015-2018
 * eoLinker是目前全球领先、国内最大的在线API接口管理平台，提供自动生成API文档、API自动化测试、Mock测试、团队协作等功能，旨在解决由于前后端分离导致的开发效率低下问题。
 * 如在使用的过程中有任何问题，欢迎加入用户讨论群进行反馈，我们将会以最快的速度，最好的服务态度为您解决问题。
 *
 * eoLinker AMS开源版的开源协议遵循GPL V3，如需获取最新的eolinker开源版以及相关资讯，请访问:https://www.eolinker.com/#/os/download
 *
 * 官方网站：https://www.eolinker.com/ 官方博客以及社区：http://blog.eolinker.com/
 * 使用教程以及帮助：http://help.eolinker.com/ 商务合作邮箱：market@eolinker.com
 * 用户讨论QQ群：707530721
 */
@RestController
@RequestMapping("/Test")
public class TestController {
    private static final int HTTP = 0;
    private static final int HTTPS = 1;

    @Resource
	private TestHistoryService testHistoryService;
	@Resource
	private ProjectService projectService;
	@Resource
	private ApiService apiService;

    @RequestMapping("/send")
    public Map<String, Object> send(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                    String params, Integer apiID, Integer projectID, int requestType) {
        switch (request.getMethod()) {
            case "GET":
                return get(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "POST":
                return post(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "DELETE":
                return delete(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "PUT":
                return put(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "HEAD":
                return head(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "PATCH":
                return patch(apiProtocol, URL, headers, params, apiID, projectID, requestType);
            case "OPTIONS":
                return options(apiProtocol, URL, headers, params, apiID, projectID, requestType);
        }
        return null;
    }

    /**
     * 参数字符串转为 List<Map<String, String>> 类型
     * @param params
     * @return
     */
    private List<Map<String, String>> paramsToListMap(String params) {
        JSONArray array = JSONArray.parseArray(params);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (int i = 0; i < array.size(); i++) {
            Map<String, String> map = JSONObject.parseObject(array.getJSONObject(i).toJSONString(), Map.class);
            result.add(map);
        }
        return result;
    }

    /**
     * 请求参数字符串转为 Map<String, String> 类型
     * @param params
     * @return
     */
    private Map<String, String> paramsToMap(String params) {
        Map<String, String> result = new HashMap<>();
        JSONArray array = JSONArray.parseArray(params);
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            String key = jsonObject.getString("key");
            String value = jsonObject.getString("value");
            result.put(key, value);
        }
        return result;
    }

    //路径参数替换

    /**
     * 替换 url 中的路径参数
     * @param url
     * @param params
     * @return
     */
    private String replacePathParams(String url, Map<String, String> params) {
        String reg = "\\{(.*?)\\}"; //定义获取路径参数匹配正则
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            String paramValue = params.getOrDefault(paramName, null);
            if (null != paramValue) {
                url = url.replace("{" + paramName + "}", paramValue);
            }
        }
        return url;
    }

    /**
     * url地址增加请求协议头
     * @param apiProtocol
     * @param url
     * @return
     */
    private String addProtocolHeader(int apiProtocol, String url) {
        if(! url.startsWith("http")){
            url = (apiProtocol == HTTP ? "http://" : "https://") + url;
        }
        return url;
    }

    /**
     * 记录测试结果
     * @param result
     * @param testParam
     * @return
     */
    private Integer addTestRecord(Map<String, Object> result, TestRequestParam testParam) {
        if (null == result || result.isEmpty()) {
            return null;
        }
        /*请求信息*/
        Map<String, Object> requestInfo = new HashMap<String, Object>();
        requestInfo.put("apiProtocol", testParam.getApiProtocol());
        requestInfo.put("method", testParam.getMethod());
        requestInfo.put("URL", testParam.getUrl());
        requestInfo.put("headers", testParam.getHeaderList());
        requestInfo.put("requestType", testParam.getRequestType());
        requestInfo.put("params", testParam.getParamList());
        /*响应信息*/
        Map<String, Object> responseInfo = new HashMap();
        responseInfo.put("headers", result.get("headers"));
        responseInfo.put("body", result.get("body"));
        responseInfo.put("httpCode", result.get("testHttpCode"));
        responseInfo.put("testDeny", result.get("testDeny"));

        // 增加测试记录
        String testTime = result.get("testTime").toString();
        Integer testId = testHistoryService.addTestHistory(testParam.getProjectId(), testParam.getApiId(),
                JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(responseInfo).toString(), testTime);

        return testId;
    }


    /**
	 * get请求测试
     *
	 */
	@RequestMapping(value = "/get", method = RequestMethod.POST)
    public Map<String, Object> get(int apiProtocol, String URL, String headers, String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> resultMap = new HashMap();    // 存储返回结果集
        if (Strings.isBlank(URL)) {
            resultMap.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            resultMap.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            resultMap.put("statusCode", "210003");
        }

        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>
        /* get 请求参数处理*/
        for (Map<String, String> param : paramList) {
            URL += URL.contains("?") ? "&" : "?";
            URL += param.getOrDefault("key", null) + "=" + param.getOrDefault("value", null);
        }
        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "GET";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);//发送测试请求

        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录

        if (null != testId) {
            resultMap.put("testID", testId);
            resultMap.put("statusCode", "000000");
            resultMap.put("testDeny", result.get("testDeny"));
            resultMap.put("testHttpCode", result.get("testHttpCode"));
            resultMap.put("testResult", result.get("testResult"));
        }
        return resultMap;
	}
	
	/**
     * delete 请求测试
     *
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Map<String, Object> delete(int apiProtocol, String URL, String headers,
                                      String params, Integer apiID, Integer projectID, int requestType) {
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }
        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>
        /* get 请求参数处理*/
        for (Map<String, String> param : paramList) {
            URL += URL.contains("?") ? "&" : "?";
            URL += param.getOrDefault("key", null) + "=" + param.getOrDefault("value", null);
        }
        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "DELETE";

        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);

        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}

	/**
	 * post请求测试
     *
	 */
	@RequestMapping(value = "/post", method = RequestMethod.POST)
    public Map<String, Object> post(int apiProtocol, String URL, String headers,
                                    String params, Integer apiID, Integer projectID, int requestType)
	{
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }
        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>

        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "POST";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}
	
	
	/**
	 * head请求测试
     *
	 */
	@RequestMapping(value = "/head", method = RequestMethod.POST)
    public Map<String, Object> head(int apiProtocol, String URL, String headers,
                                    String params, Integer apiID, Integer projectID, int requestType) {
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }
        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>
        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头

        String method = "HEAD";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);

        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}
	
	/**
	 * patch请求测试
     *
	 */
	@RequestMapping(value = "/patch", method = RequestMethod.POST)
    public Map<String, Object> patch(int apiProtocol, String URL, String headers,
                                     String params, Integer apiID, Integer projectID, int requestType) {
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }

        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>

        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "PATCH";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);

        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}
	
	/**
	 * put请求测试
	 */
	@RequestMapping(value = "/put", method = RequestMethod.POST)
    public Map<String, Object> put(int apiProtocol, String URL, String headers,
                                   String params, Integer apiID, Integer projectID, int requestType) {
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }
        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>

        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "PUT";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}
	
	/**
	 * options请求测试
     *
	 */
	@RequestMapping(value = "/options", method = RequestMethod.POST)
    public Map<String, Object> options(int apiProtocol, String URL, String headers,
                                       String params, Integer apiID, Integer projectID, int requestType) {
		Map<String, Object> map = new HashMap<String, Object>();
        if (Strings.isBlank(URL)) {
            map.put("statusCode", "210001");
        }
        if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        }
        if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        }
        URL = replacePathParams(URL, paramsToMap(params)); //替换路径参数
        List<Map<String, String>> headerList = paramsToListMap(headers);   // headers 字符串转 List<Map>
        List<Map<String, String>> paramList = paramsToListMap(params);     // params 参数字符串转 List<Map>

        String completeURL = addProtocolHeader(apiProtocol, URL); //增加协议头
        String method = "OPTIONS";
        Proxy proxy = new Proxy();
        Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
        TestRequestParam testRequestParam = new TestRequestParam(URL, apiID, projectID, apiProtocol, method, requestType, headerList, paramList);
        Integer testId = addTestRecord(result, testRequestParam);//增加测试记录
        if (testId != null) {
            map.put("statusCode", "000000");
            map.put("testHttpCode", result.get("testHttpCode"));
            map.put("testDeny", result.get("testDeny"));
            map.put("testResult", result.get("testResult"));
            map.put("testID", testId);
        }
		return map;
	}

	/**
	 * 删除测试记录
     *
	 */
	@RequestMapping(value = "/deleteTestHistory", method = RequestMethod.POST)
	public Map<String, Object> deleteTestHistory(HttpServletRequest request, Integer testID, Integer projectID) {
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		Integer userID = (Integer) session.getAttribute("userID");
		Partner partner = projectService.getProjectUserType(userID, projectID);
		if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
			map.put("statusCode", "100002");
		}

		boolean result = testHistoryService.deleteTestHistory(projectID, userID, testID);
        map.put("statusCode", result ? "000000" : "210000");
		return map;
	}

	/**
	 * 清空测试记录
     *
	 */
	@RequestMapping(value = "/deleteAllTestHistory", method = RequestMethod.POST)
	public Map<String, Object> deleteAllTestHistory(HttpServletRequest request, Integer apiID, Integer projectID)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		Integer userID = (Integer) session.getAttribute("userID");
		Partner partner = projectService.getProjectUserType(userID, projectID);
		if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
			map.put("statusCode", "100002");
			return map;
		}
        boolean result = testHistoryService.deleteAllTestHistory(projectID, userID, apiID);
		map.put("statusCode", result ? "000000" : "210000");
		return map;
	}

	/**
	 * 添加测试记录
     *
	 */
	@RequestMapping(value = "/addTestHistory", method = RequestMethod.POST)
	public Map<String, Object> addTestHistory(HttpServletRequest request, Integer apiID, String requestInfo,
			String resultInfo) {
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession(true);
		Integer userID = (Integer) session.getAttribute("userID");
		Integer projectID = apiService.getProjectID(apiID);
		Partner partner = projectService.getProjectUserType(userID, projectID);
		if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
			map.put("statusCode", "100002");
			return map;
		}
        String testTime = DateUtil.formatAll(new Date());
        Integer result = testHistoryService.addTestHistory(projectID, apiID, requestInfo, resultInfo, testTime);
        if (result == null) {
            map.put("statusCode", "210000");
            return map;
        }
        map.put("testID", result);
        map.put("statusCode", "000000");
		return map;
	}
}
