package com.orby.cmn.upload.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public interface FileService {
    /**
    * description:  保存数据
    * @author HuangJian
    * @param jsonFile
    * @return int
    * @create 2020-11-22
    */
    public int save(JSONObject jsonFile);

    /**
    * description:  根据fileKey 获取最大  ShardIndex 如果不存在 说明没有上传过
     * 如果存在 说明上传的到这次之后就没有再传了
    * @author HuangJian
    * @param fileKey
    * @return  返回空对象 说名没有查询到
    * @create 2020-11-22
    */
    public JSONObject getByFileKeyByMaxShardIndex(String fileKey);
    /**
    * description:  删除本地的分片信息  .pdf.1 到  shardTotal
    * @author HuangJian
    * @param basePath
    * @param shardTotal
    * @return int
    * @create 2020-11-26
    */
    public int deleteLocalFile(Integer shardTotal,String basePath);
}
