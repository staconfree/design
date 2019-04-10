package com.roy.github.learn.http;

import com.roy.github.learn.javabase.util.FileUtil;
import com.roy.github.learn.javabase.util.IOUtilEx;
import com.roy.github.learn.javabase.util.ZipUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * <dependency>
 *             <groupId>org.apache.httpcomponents</groupId>
 *             <artifactId>httpmime</artifactId>
 *             <version>4.3.6</version>
 *         </dependency>
 * 使用http下载或者上传文件
 */
public class DownloadUploadFileByHttp {

    /**
     * 文件服务器URL地址，形如："http://localhost:8080/";
     */
    private String remoteFileSystemUrl = "";
    /**
     * 客户端版本号
     */
    private String clientVer = "";
    private String legalPerson = "";
    private String userName = "";
    /**
     * 超时时间
     */
    private int connectTimeout = 30 * 1000;
    private int connectionRequestTimeout = 30 * 1000;
    private int socketTimeout = 10 * 60 * 1000;

    /**
     * 空文件对应的hash值
     */
    public final static String NULL_FILE_HASH = "fd06dddba2b340a36ac0912d148622f8";

    private static final long MAX_UPLOAD_SIZE = 2 * 1024L * 1024L * 1024L; // 2G

    /**
     * zip文件后缀名
     */
    public final static String ZIP_SUFFIX = ".gz";
    /**
     * 下载文件单次请求
     *
     * @param fileId
     * @param locationFilePath
     * @param hashValues
     * @return 返回文件是否下载成功
     * @throws Exception
     */
    private String downloadFileFromHttp(String fileId, String locationFilePath, List<String> hashValues)
            throws Exception {
        String remoteFileUrl =
                this.remoteFileSystemUrl + "/download" + "?fileId=" + fileId + "&clientVer="
                        + clientVer + "&legalPerson=" + this.legalPerson + "&userName=" + this.userName;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        String md5Value = null;
        FileOutputStream out = null;

        String zipFilePath = "";
        try {
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(this.socketTimeout)
                    .setConnectTimeout(this.connectTimeout)
                    .setConnectionRequestTimeout(this.connectionRequestTimeout).build();
            HttpGet httpGet = new HttpGet(remoteFileUrl);
            httpGet.setConfig(config);

            httpResponse = httpclient.execute(httpGet);
            zipFilePath = locationFilePath + "_" + System.currentTimeMillis() + ".gz";
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream in = httpEntity.getContent();
            out = new FileOutputStream(new File(zipFilePath));
            md5Value = IOUtilEx.copyBytes(in, out);

            // MD5校验
            if (hashValues.size() > 0 && hashValues.get(0).equals(md5Value)) {
                // 加压文件后，删除压缩文件
                ZipUtil.unZipFile(zipFilePath, locationFilePath);
            } else {
                // 删除压缩文件
                if (md5Value.equalsIgnoreCase(NULL_FILE_HASH)) {
                    return "FILE_NOT_EXSIT";
                } else {
                    return "HASH_ERROR" + "input:" + hashValues.get(0) + ",cal:" + md5Value;
                }
            }
        } catch (Exception e) {
            throw e;
        }finally {
            if (null != out) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {

                }
            }

            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {

                }
            }

            if (null != httpclient) {
                try {
                    httpclient.close();
                } catch (IOException e) {

                }
            }

            // 判断压缩文件是否存在，删除压缩文件
            if (zipFilePath!=null&&!"".equals(zipFilePath.trim())) {
                ZipUtil.deleteTmpFile(zipFilePath);
            }
        }
        return "ok";
    }


    public class FileHashInfo {

        private String fileId;

        /**
         * Design flaw!!! As to store chunked files, just a string value is enough!!!
         * However, keeping get along with it!!!
         */
        private List<String> hashValues;

        /**
         * Map of chunked files hash information, key: chunked file source file name, value: chunked file hash information
         */
        private Map<String, ChunkedFileHashInfo> chunkedFileHashInfoMap;

        public FileHashInfo() {
            fileId = "";
            hashValues = new ArrayList<String>();
            chunkedFileHashInfoMap = new HashMap<>();
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public List<String> getHashValues() {
            return hashValues;
        }

        public void setHashValues(List<String> hashValues) {
            this.hashValues = hashValues;
        }

        public Map<String, ChunkedFileHashInfo> getChunkedFileHashInfoMap() {
            return chunkedFileHashInfoMap;
        }

        public void setChunkedFileHashInfoMap(Map<String, ChunkedFileHashInfo> chunkedFileHashInfoMap) {
            this.chunkedFileHashInfoMap = chunkedFileHashInfoMap;
        }

    }

    public class ChunkedFileHashInfo {

        private String fileId = "";

        private int chunkIndex = -1;

        private List<String> hashValues = new ArrayList<>();

        private String sourceFileName = "";

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public void setChunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
        }

        public List<String> getHashValues() {
            return hashValues;
        }

        public void setHashValues(List<String> hashValues) {
            this.hashValues = hashValues;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public ChunkedFileHashInfo() {

        }

        public ChunkedFileHashInfo(String fileId, int chunkIndex, List<String> hashValues, String sourceFileName) {
            this.fileId = fileId;
            this.chunkIndex = chunkIndex;
            this.hashValues = hashValues;
            this.sourceFileName = sourceFileName;
        }

    }

    public enum FileTypeEnum {

        TEMPFILE("0", "临时文件，支持老版本。这种文件类型以后会删除"),
        AUDIO("1", "音频文件"),
        VEDIO("2", "视频文件"),
        TEXTFILE("3","文本文件，会进行压缩后上传"),
        PICTURE("4","图片文件"),
        OTHER("5","其他类型文件");

        /**
         * 编号
         */
        private String id;

        /**
         * 描述
         */
        private String desc;

        FileTypeEnum(String id, String desc) {
            this.id = id;
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public enum SaveTypeEnum {

        FOREVER("0", "永久存储"),
        YEAR("1", "按年存储"),
        MONTH("2","按月存储"),
        DAY("3", "按天存储");

        /**
         * 编号
         */
        private String id;

        /**
         * 描述
         */
        private String desc;

        SaveTypeEnum(String id, String desc) {
            this.id = id;
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    /**
     * @param fileType         文件类型
     * @param locationFilePath 文件在本地保存路径
     * @param saveType         存储类型
     * @param count            数量
     * @return
     * @throws Exception
     */
    private FileHashInfo putSingle(FileTypeEnum fileType, String locationFilePath,
                                   SaveTypeEnum saveType, int count) throws Exception {
        FileHashInfo fileHashInfo = new FileHashInfo();

        File file = new File(locationFilePath);
        if (!file.isFile() || !file.exists()) {
            throw new Exception("Error Put, locationFilePath:" + locationFilePath);
        }

        // Check whether chunking is needed
        long fileSize = FileUtil.getFileSize(locationFilePath);
        if (fileSize > MAX_UPLOAD_SIZE) {
            // Need chunking
            List<String> chunkedFilePathList = FileUtil.split(locationFilePath, MAX_UPLOAD_SIZE);
            if (chunkedFilePathList==null||chunkedFilePathList.isEmpty()) {
                throw new Exception("Can NOT chunk big file " + locationFilePath + " correctly!~");
            }

            int chunks = chunkedFilePathList.size();
            String bigFileId = "";
            for (String chunkedFilePath : chunkedFilePathList) {
                String zipFilePath = chunkedFilePath + "_" + System.currentTimeMillis() + ZIP_SUFFIX;
                File zipFile = new File(zipFilePath);
                if (zipFile.exists()) {
                    throw new Exception("Chunking big file " + locationFilePath + ", but temp zipped file " + zipFilePath + " exists");
                }

                ZipUtil.zipFile(chunkedFilePath, zipFilePath);

                String url = remoteFileSystemUrl + "/upload";
                CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = null;
                try {
                    HttpPost httppost = new HttpPost(url);
                    RequestConfig config =
                            RequestConfig.custom().setSocketTimeout(this.socketTimeout)
                                    .setConnectTimeout(this.connectTimeout)
                                    .setConnectionRequestTimeout(this.connectionRequestTimeout).build();
                    httppost.setConfig(config);
                    // Zipped file content
                    FileBody bin = new FileBody(zipFile);
                    // File type
                    StringBody bodyFileType = new StringBody(fileType.getId(), ContentType.TEXT_PLAIN);
                    // Save type
                    StringBody bodySaveType = new StringBody(saveType.getId(), ContentType.TEXT_PLAIN);
                    // Save count
                    StringBody bodySaveCount = new StringBody(String.valueOf(count), ContentType.TEXT_PLAIN);
                    // Big size (Caution!!!)
                    StringBody bodyFileLength = new StringBody(String.valueOf(fileSize), ContentType.TEXT_PLAIN);
                    // Legal person
                    StringBody bodyLegalPerson = new StringBody(legalPerson, ContentType.TEXT_PLAIN);
                    // User name
                    StringBody bodyUserName = new StringBody(userName, ContentType.TEXT_PLAIN);
                    // Chunked
                    StringBody bodyChunked = new StringBody(String.valueOf(true), ContentType.TEXT_PLAIN);
                    // Chunk size (Caution!!!)
                    StringBody bodyChunkSize = new StringBody(String.valueOf(FileUtil.getFileSize(zipFilePath)), ContentType.TEXT_PLAIN);
                    // Chunks
                    StringBody bodyChunks = new StringBody(String.valueOf(chunks), ContentType.TEXT_PLAIN);
                    // Chunk index
                    StringBody bodyChunkIndex = new StringBody(String.valueOf(chunkedFilePathList.indexOf(chunkedFilePath)), ContentType.TEXT_PLAIN);
                    // Big file id
                    StringBody bodyFileId = new StringBody(bigFileId, ContentType.TEXT_PLAIN);

                    HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("fileContent", bin)
                            .addPart("saveType", bodySaveType).addPart("saveCount", bodySaveCount)
                            .addPart("fileType", bodyFileType).addPart("fileLength", bodyFileLength)
                            .addPart("legalPerson", bodyLegalPerson).addPart("userName", bodyUserName)
                            .addPart("chunked", bodyChunked).addPart("chunkSize", bodyChunkSize)
                            .addPart("chunks", bodyChunks).addPart("chunkIndex", bodyChunkIndex)
                            .addPart("fileId", bodyFileId).build();
                    httppost.setEntity(reqEntity);
                    response = httpclient.execute(httppost);

                    HttpEntity resEntity = response.getEntity();
                    String jsonMsg = EntityUtils.toString(resEntity);
                    EntityUtils.consume(resEntity);
                    //
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
//                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//                    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//
//                    WebMessage<?> webMessage = mapper.readValue(jsonMsg, WebMessage.class);
//                    if (null != webMessage && null != webMessage.getResult()) {
//                        Map<?, ?> map = (Map<?, ?>) webMessage.getResult();
//                        Object obj = map.get("fileId");
//                        if (obj instanceof String) {
//                            String fileId = (String) obj;
//                            fileHashInfo.setFileId(fileId);
//                            if (StringUtils.isBlank(bigFileId)) {
//                                bigFileId = fileId;
//                            } else {
//                                if (!StringUtils.equals(bigFileId, fileId)) {
//                                    throw new Exception("Confused file id for big file, current=" + bigFileId
//                                            + " latest=" + fileId);
//                                }
//                            }
//                        }
//
//                        // Disgusting hashValues MUST NOT be empty
//                        obj = map.get("hashValues");
//                        if (obj instanceof List<?>) {
//                            @SuppressWarnings("unchecked")
//                            List<String> hashValues = (List<String>) obj;
//                            fileHashInfo.setHashValues(hashValues);
//                        }
//
//                        obj = map.get("chunkedFileHashInfoMap");
//                        if (obj instanceof Map<?, ?>) {
//                            Map<String, LinkedHashMap> dataMap = (Map<String, LinkedHashMap>) obj;
//                            if (!dataMap.isEmpty()) {
//                                if (!dataMap.containsKey(zipFile.getName())) {
//                                    throw new Exception("Can NOT find hash information associated with " + zipFile.getName()
//                                            + ", please check with it!~");
//                                }
//
//                                Map<String, ChunkedFileHashInfo> chunkedFileHashInfoMap = new HashMap<>();
//                                for (Map.Entry<String, LinkedHashMap> entry : dataMap.entrySet()) {
//                                    // Construct chunked file hash information from linked hash map result
//                                    ChunkedFileHashInfo chunkedFileHashInfo = new ChunkedFileHashInfo();
//                                    LinkedHashMap<String, ?> linkedHashMap = entry.getValue();
//                                    for (Map.Entry<String, ?> subEntry : linkedHashMap.entrySet()) {
//                                        if (StringUtils.equals("fileId", subEntry.getKey())) {
//                                            chunkedFileHashInfo.setFileId((String) subEntry.getValue());
//                                        } else if (StringUtils.equals("chunkIndex", subEntry.getKey())) {
//                                            chunkedFileHashInfo.setChunkIndex((Integer) subEntry.getValue());
//                                        } else if (StringUtils.equals("hashValues", subEntry.getKey())) {
//                                            chunkedFileHashInfo.setHashValues((List<String>) subEntry.getValue());
//                                        } else if (StringUtils.equals("sourceFileName", subEntry.getKey())) {
//                                            chunkedFileHashInfo.setSourceFileName((String) subEntry.getValue());
//                                        }
//                                    }
//
//                                    if (StringUtils.equals(zipFile.getName(), entry.getKey())) {
//                                        String zipFileMd5 = MD5Util.getMD5(zipFilePath);
//                                        if (chunkedFileHashInfo.getHashValues().size() >= 1
//                                                && !zipFileMd5.equals(chunkedFileHashInfo.getHashValues().get(0))) {
//                                            throw new Exception("Zip File Md5 check failed, zipMd5 : " + zipFileMd5
//                                                    + "\tServerMd5:" + chunkedFileHashInfo.getHashValues().get(0));
//                                        }
//                                    }
//                                    chunkedFileHashInfoMap.put(entry.getKey(), chunkedFileHashInfo);
//                                }
//                                fileHashInfo.setChunkedFileHashInfoMap(chunkedFileHashInfoMap);
//                            }
//                        }
//                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (null != httpclient) {
                        httpclient.close();
                    }

                    if (null != response) {
                        response.close();
                    }
                    // Delete zipped file
                    ZipUtil.deleteTmpFile(zipFilePath);
                    // Delete chunked file
                    ZipUtil.deleteTmpFile(chunkedFilePath);
                }
            }
        } else {
            // Normally whole file uploading directly
            // 生成压缩文件后的zip文件
            String target =
                    locationFilePath + "_"+ System.currentTimeMillis()
                            + ZIP_SUFFIX;

            File fileTarget = new File(target);
            if (fileTarget.exists()) {
                throw new Exception("PutSingle temp file exist, target:" + target);
            }

            ZipUtil.zipFile(locationFilePath, target);

            String url = this.remoteFileSystemUrl + "/upload";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = null;

            try {
                HttpPost httppost = new HttpPost(url);
                RequestConfig config =
                        RequestConfig.custom().setSocketTimeout(this.socketTimeout)
                                .setConnectTimeout(this.connectTimeout)
                                .setConnectionRequestTimeout(this.connectionRequestTimeout).build();
                httppost.setConfig(config);

                // 文件具体内容
                FileBody bin = new FileBody(fileTarget);

                // 文件类型
                StringBody bodyFileType = new StringBody(fileType.getId(), ContentType.TEXT_PLAIN);

                // 文件保存类型（按年，按月，按日）
                StringBody bodySaveType = new StringBody(saveType.getId(), ContentType.TEXT_PLAIN);

                // 文件保存时长（几年，几月，几日）
                StringBody bodySaveCount = new StringBody(String.valueOf(count), ContentType.TEXT_PLAIN);

                // 文件长度
                StringBody bodyFileLength =
                        new StringBody(String.valueOf(fileTarget.length()), ContentType.TEXT_PLAIN);

                // 法人
                StringBody bodyLegalPerson = new StringBody(legalPerson, ContentType.TEXT_PLAIN);

                // 用户名
                StringBody bodyUserName = new StringBody(userName, ContentType.TEXT_PLAIN);

                HttpEntity reqEntity =
                        MultipartEntityBuilder.create().addPart("fileContent", bin)
                                .addPart("saveType", bodySaveType).addPart("saveCount", bodySaveCount)
                                .addPart("fileType", bodyFileType).addPart("fileLength", bodyFileLength)
                                .addPart("legalPerson", bodyLegalPerson).addPart("userName", bodyUserName).build();

                httppost.setEntity(reqEntity);
                response = httpclient.execute(httppost);

                HttpEntity resEntity = response.getEntity();

                String jsonMsg = EntityUtils.toString(resEntity);
                EntityUtils.consume(resEntity);

//                ObjectMapper mapper = new ObjectMapper();
//                mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
//                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//                mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//
//                WebMessage<?> webMessage = mapper.readValue(jsonMsg, WebMessage.class);
//
//                if (null != webMessage && null != webMessage.getResult()) {
//                    Map<?, ?> map = (Map<?, ?>) webMessage.getResult();
//
//                    Object obj = map.get("fileId");
//                    if (obj instanceof String) {
//                        String fileId = (String) obj;
//                        fileHashInfo.setFileId(fileId);
//                    }
//
//                    obj = map.get("hashValues");
//                    if (obj instanceof List<?>) {
//                        @SuppressWarnings("unchecked")
//                        List<String> hashValues = (List<String>) obj;
//                        fileHashInfo.setHashValues(hashValues);
//                    }
//                }
//                String zipFileMd5 = MD5Util.getMD5(target);
//                if (fileHashInfo.getHashValues().size() >= 1 && !zipFileMd5.equals(fileHashInfo.getHashValues().get(0))) {
//                    throw new Exception("Zip File Md5 check failed, zipMd5 : " + zipFileMd5 + "\tServerMd5:" + fileHashInfo.getHashValues().get(0));
//                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (null != httpclient) {
                    httpclient.close();
                }

                if (null != response) {
                    response.close();
                }

                // 删除临时文件
                ZipUtil.deleteTmpFile(target);
            }
        }
        return fileHashInfo;
    }


}
