package com.eolinker.pojo;

import java.util.List;
import java.util.Map;

/**
 * @author wangjunxia
 * @date 2019-01-16
 */
public class TestRequestParam {
    private String url;
    private int apiId;
    private int projectId;
    private int apiProtocol;
    private String method;
    private int requestType;
    List<Map<String, String>> headerList;
    List<Map<String, String>> paramList;

    public TestRequestParam(String url, int apiId, int projectId, int apiProtocol, String method, int requestType, List<Map<String, String>> headerList, List<Map<String, String>> paramList) {
        this.url = url;
        this.apiId = apiId;
        this.projectId = projectId;
        this.apiProtocol = apiProtocol;
        this.method = method;
        this.requestType = requestType;
        this.headerList = headerList;
        this.paramList = paramList;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getApiProtocol() {
        return apiProtocol;
    }

    public void setApiProtocol(int apiProtocol) {
        this.apiProtocol = apiProtocol;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public List<Map<String, String>> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<Map<String, String>> headerList) {
        this.headerList = headerList;
    }

    public List<Map<String, String>> getParamList() {
        return paramList;
    }

    public void setParamList(List<Map<String, String>> paramList) {
        this.paramList = paramList;
    }

}
