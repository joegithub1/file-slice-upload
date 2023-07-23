package com.orby.cmn.upload.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orby.cmn.upload.service.FileService;
import com.orby.cmn.upload.utils.RedisServer;
import com.orby.cmn.upload.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    public FileService fileService;

    public static final String BUSINESS_NAME = "普通分片上传";

    // 设置上传路径
    @Value("${file.basePathWindows}")
    private String basePathWindows;

    @Value("${file.basePathLiunx}")
    private String basePathLiunx;

    private String basePath;


    @PostConstruct
    public void prev(){
        String osName = System.getProperty("os.name");
        log.info("操作系统：{}",osName);
        if(!StringUtils.isEmpty(osName) && osName.length() > 3){
            osName = osName.substring(0, 3);
            if("win".equalsIgnoreCase(osName)){
                basePath = basePathWindows;
            }else{
                basePath = basePathLiunx;
            }
            File file = new File(basePath);
            if(!file.exists()){
                file.mkdirs();//创建文件夹路径
            }
        }
    }

    //设置下载访问路径
    @Value("${file.baseDownPath}")
    private String baseDownPath;
    /**
    * description:  访问文件
    * @author HuangJian
    * @param jsonParam  文件唯一
    * @return com.orby.cmn.upload.utils.Result
    * @create 2020-11-26
    */
    @RequestMapping("/downLoad")
    public Result downLoad(@RequestBody String jsonParam){
        JSONArray  result = new JSONArray();
        JSONObject obj = new JSONObject();
        JSONObject jsonObj = JSONObject.parseObject(jsonParam);
        //文件唯一名
        String fileKey = String.valueOf(jsonObj.getOrDefault("fileKey",""));
        //文件后缀
        String suffix = String.valueOf(jsonObj.getOrDefault("suffix","mp4"));
        if(StringUtils.isEmpty(fileKey)){
            return Result.fail("文件fileKey不能为空!");
        }
        if(StringUtils.isEmpty(suffix)){
            return Result.fail("文件后缀不能为空!");
        }
        StringBuffer pathStr = new StringBuffer("");
        pathStr.append(basePath);
        pathStr.append(fileKey).append(".").append(suffix.toLowerCase());
        log.info("检查文件路径是否存在:::{}",pathStr.toString());
        File file = new File(pathStr.toString());
        if(!file.exists()){
            obj.put("EFILE_DIR","");
            obj.put("WITNESS_STATUS","");
            obj.put("FILE_PATH","");
            obj.put("INIT_DATE","");
            obj.put("EXIST","false");
            obj.put("url","");
            obj.put("REMARK","");//驳回原因
//            return Result.fail("文件不存在");
        }else{

            obj.put("EFILE_DIR",baseDownPath);
            obj.put("WITNESS_STATUS","");
            obj.put("FILE_PATH",fileKey+"."+suffix);
            obj.put("INIT_DATE","");
            obj.put("EXIST","true");
            obj.put("REMARK","");//驳回原因
            pathStr = new StringBuffer("");
            pathStr.append(baseDownPath).append(File.separator).append(fileKey)
                    .append(".").append(suffix);
            obj.put("url",pathStr.toString());
        }

        result.add(obj);
        return Result.ok("成功！",result);
    }
    /**
     * 上传 (分片顺序上传)
     * @param file 上传的文件
     * @param shardIndex 当前是第几块文件
     * @param shardSize  每个分片大小
     * @param shardTotal 分片总数
     * @param fileSize 文件总大小
     * @param key 文件标识 md5
     * @param suffix 文件后缀
     * @param showName 文件的真实名称
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @RequestMapping("/upload")
    public Result upload(MultipartFile file,
                                     Integer shardIndex,
                                     String shardSize,
                                     Integer shardTotal,
                                     Integer fileSize,
                                     String key,
                                     String suffix,
                                    String showName
                         ) throws IOException, InterruptedException {
        //Map<String,Object> resultMap = new HashMap<>();
        log.info("shardIndex:{}，shardSize:{}，shardTotal:{}，key:{}",shardIndex,shardSize,shardTotal,key);
        log.info("上传文件开始");
        //文件的名称
        //String name = UUID.randomUUID().toString().replaceAll("-", "");
        // 获取文件的扩展名  由于分片了从file中获取不到文件后缀了 由前端传
        //String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        Integer shardSizeInt = 0;
        if(StringUtils.isEmpty(shardSize)){//如果分片大小为空 获取当前传的文件大小
            shardSizeInt = Integer.valueOf(file.getSize()+"");
        }else{
            shardSizeInt = Integer.valueOf(shardSize);
        }
        suffix = suffix.toLowerCase();
        //设置图片新的名字
        // course\6sfSqfOwzmik4A4icMYuUe.mp4
        //如果这个文件存在 ， 说明之前已经上传过
        String fileName = new StringBuffer().append(key).append(".").append(suffix).toString();
        File fEx = new File(basePath,fileName);
        if(fEx.exists()){//文件已经存在了，不需要再次上传
//            log.info("{}已经存在了，不需要再次上传了！",fileName);
//            return Result.ok(Result.FILE_EXIS,"秒传成功！");
            //相同的文件可以覆盖，前端控制是否传
            fEx.delete();
        }

        //这个是分片的名字
        String localfileName = new StringBuffer(fileName)
                .append(".")
                .append(shardIndex)
                .toString(); // course\6sfSqfOwzmik4A4icMYuUe.mp4.1
        log.info("分片的名称：localfileName::"+localfileName);
        log.info("文件名称：：："+fileName);
        try {

            // 以绝对路径保存重名命后的文件
            File targeFile=new File(basePath,localfileName);
            if(targeFile.exists()){
                //说明 xxxx.pdf.1 xxxx.pdf.N 已经存在了
                //return Result.ok("已经存在了，不需要再次上传！");
                return Result.ok(Result.FILE_SHARD_EXIS,"当前分片已经存在，不需要再次上传！");
            }
            //上传这个文件
            file.transferTo(targeFile);

            JSONObject objFile = new JSONObject();
            //objFile.put("path",basePath+localfileName);
            objFile.put("name",fileName);
            objFile.put("showName",showName);//真实显示名称
            objFile.put("suffix",suffix);
            objFile.put("size",fileSize);
            //objFile.put("createdAt",System.currentTimeMillis());
            //objFile.put("updatedAt",System.currentTimeMillis());
            objFile.put("shardIndex",shardIndex);
            objFile.put("shardSize",shardSizeInt);
            objFile.put("shardTotal",shardTotal);
            objFile.put("fileKey",key);
            log.info(objFile.toString());

            int save = fileService.save(objFile);
            log.info("{}文件上传到了第{}次，总共{}次，上传结果:{}",key,shardIndex,shardTotal,save);
            Map<String, String> fileKeyMap = RedisServer.getHashFromRedis(RedisServer.FILE_KEY+key, "", RedisServer.jedisIndex_1);
            Integer succCount = 1;//默认已经上传了
            if(null != fileKeyMap){
                succCount = fileKeyMap.keySet().size();//获取已经上传的次数
            }
            //所有的分片都已经上传完成 做逻辑处理  或者 只有一次上传
            if(succCount == shardTotal || 1 == shardTotal){
                String fPath = basePath+fileName;
                objFile.put("path",fPath);
                try {
                    this.merge(objFile);
                } catch (FileNotFoundException e) {
                    log.error("合并分片异常：{}",e.getMessage());
                }
                new Thread(){
                    public void run(){
                        //防止文件IO流没有关闭 而导致删除失败
                        try {
                            Thread.sleep(2000);//缓2秒删除本地分片文件
                        } catch (InterruptedException e) {
                        }
                        int delCount = fileService.deleteLocalFile(shardTotal, basePath + fileName);
                        log.info("{}总共删除了本地文件{}个",fileName,delCount);

                        //文件上传成功之后 删除这个redis key
                        RedisServer.deleteByOneKey(RedisServer.FILE_KEY+key,RedisServer.jedisIndex_1);
                    }
                }.start();

                //合并后 分片信息删除
                //RedisServer.deleteByOneKey(RedisServer.FILE_KEY+fileName,RedisServer.jedisIndex_1);

                //防止本地文件删了，redis没有删除的情况，暂时不写
                //JSONObject successObj = new JSONObject();
                //successObj.put("status","1");//表示
                //successObj.put("fileId",fileName);
                //RedisServer.setValue(RedisServer.jedisIndex_1,RedisServer.SUCCESS_FILE_KEY+fileName,successObj.toJSONString());
            }
            //resultMap.put("fileName",fileName);
            return Result.ok("上传成功！");
        } catch (IOException e) {
            log.error("上传失败 IOException:{}",e);
            return Result.fail("上传失败！");
        } catch (IllegalStateException e) {
            log.error("上传失败 IllegalStateException:{}",e);
            return Result.fail("上传失败！");
        }
    }

    @RequestMapping("/check")
    public Result check(@RequestBody String fileKey){
        if(StringUtils.isEmpty(fileKey)){
            return Result.fail("fileKey不能为空!");
        }
        try {
            JSONObject resultObj = fileService.getByFileKeyByMaxShardIndex(fileKey);
            JSONArray arr = new JSONArray();
            if(null != resultObj){
                arr.add(resultObj);
            }
            return Result.ok("查询成功！",arr);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询异常！");
        }
    }


    /**
     *
     * 合并分页
     */
    private int merge(JSONObject paramObj) throws FileNotFoundException {
        //合并分片开始
        log.info("分片合并开始");
        String path = paramObj.getString("path"); //获取到的路径 没有.1 .2 这样的东西
        //截取视频所在的路径
        path = path.replace(basePath,"");
        Integer shardTotal= paramObj.getInteger("shardTotal");
        File newFile = new File(basePath + path);
        FileOutputStream outputStream = new FileOutputStream(newFile,true); // 文件追加写入
        FileInputStream fileInputStream = null; //分片文件
        byte[] byt = new byte[10 * 1024 * 1024];
        int len;
        try {
            for (int i = 0; i < shardTotal; i++) {
                // 读取第i个分片
                File file = new File(basePath + path + "." + (i + 1));
                fileInputStream = new FileInputStream(file); //  course\6sfSqfOwzmik4A4icMYuUe.mp4.1
                while ((len = fileInputStream.read(byt)) != -1) {
                    outputStream.write(byt, 0, len);
                }
            }
            outputStream.flush();
            log.info("分片结束了");
            //告诉java虚拟机去回收垃圾 至于什么时候回收  这个取决于 虚拟机的决定
            System.gc();
            return 1;
        } catch (IOException e) {
            log.error("分片合并异常", e);
            return 0;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if(null != outputStream){
                    outputStream.close();
                }
                log.info("IO流关闭");
            } catch (Exception e) {
                log.error("IO流关闭", e);
            }
        }

    }

}
