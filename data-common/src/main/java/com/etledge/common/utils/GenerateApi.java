package com.etledge.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.TableRenderData;
import com.deepoove.poi.data.Tables;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateApi {

    public static void main(String[] args) throws IOException {
        method();
    }

    private static void method() throws IOException {
        // 读取指定路径的文件内容到字符串fileString
        String fileString = readFile("D:\\tmp\\api.json");
        JSONArray jsonObject = JSON.parseArray(fileString);

        if (Objects.nonNull(jsonObject)) {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, List<Map<String, Object>>> datasMap = new HashMap<>();
            int i = 0;
            for (Object item : jsonObject) {
                JSONObject obj = (JSONObject) item;
                JSONArray listjson = obj.getJSONArray("list");
                for (Object object : listjson) {
                    JSONObject obj2 = (JSONObject) object;
                    String reqBodyOtherObj = obj2.getString("req_body_other");
                    //创建请求表格
                    TableRenderData requestList = Tables.create();
                    //创建响应表格
                    TableRenderData responseList = Tables.create();
                    // 创建文档参数的表头行
                    RowRenderData header = Rows.create(
                            "字段",
                            "类型",
                            "描述",
                            "必填"
                    );
                    // 将表头添加到请求表格
                    requestList.addRow(header);
                    // 将表头添加到响应表格
                    responseList.addRow(header);
                    Map<String, String> stringMap = new HashMap<>(16);
                    if (StringUtils.isNotBlank(reqBodyOtherObj)) {
                        JSONObject reqBodyOther = cleanData(reqBodyOtherObj);
                        if (Objects.nonNull(reqBodyOther)) {
                            String propertiesObj = reqBodyOther.getString("properties");
                            if (StringUtils.isNotBlank(propertiesObj)) {
                                JSONObject rquestProperties = cleanData(propertiesObj);
                                if (Objects.nonNull(rquestProperties)) {
                                    JSONArray required = reqBodyOther.getJSONArray("required");
                                    if (Objects.nonNull(required) && required.size() > 0) {
                                        List<String> javaList = required.toJavaList(String.class);
                                        // 使用Stream API将javaList转换为Map，其中键和值都是列表中的元素
                                        stringMap = javaList.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
                                    }
                                    //处理请求报文
                                    setFileBody(rquestProperties, requestList, "", stringMap);
                                }
                            }
                        }
                    }
                    String resBodyObj = obj2.getString("res_body");

                    if (StringUtils.isNotBlank(resBodyObj)) {
                        JSONObject responseJson = cleanData(resBodyObj);
                        if (Objects.nonNull(responseJson)) {
                            Object propertiesObj = responseJson.get("properties");
                            if (Objects.nonNull(propertiesObj)) {

                                JSONObject responseProperties = JSON.parseObject(propertiesObj.toString());
                                //处理响应报文
                                setFileBody(responseProperties, responseList, "", stringMap);
                            }
                        }
                    }
                    String successCase = obj.getString("successCase");
                    int finalI = ++i;
                    list.add(new HashMap<String, Object>() {{
                        //设置文档中的接口序号
                        put("index", finalI);
                        //接口名称
                        put("name", obj2.getString("title"));
                        //接口url
                        put("url", obj2.getString("path"));
                        //接口访问类型 例：GET、POST
                        put("requestType", obj2.getString("method"));
                        //请求参数信息
                        put("requestJson", requestList);
                        //访问参数信息
                        put("responseJson", responseList);
                        //响应示例
                        put("successCase", successCase);
                    }});
                }
            }
            datasMap.put("item", list);
            //根据指定的模板生成接口文档，template_input.docx 需要提前准备好，template_output.docx会在代码执行后在如下代码中指定的路径自动生成
            XWPFTemplate.compile("D:\\tmp\\API接口模板.docx").render(datasMap).writeToFile("D:\\tmp\\数据集成模块接口.docx");
        }

    }


    public static void setFileBody(JSONObject obj, TableRenderData renderList, String prefix, Map<String, String> stringMap) {
        for (String key : obj.keySet()) {
            // 获取每个键对应的值
            Object value = obj.get(key);
            //默认可为空
            boolean isNull = true;
            if (Objects.nonNull(stringMap)) {
                String required = stringMap.get(key);
                if (!StringUtils.isBlank(required)) {
                    isNull = false;
                }
            }
            String valueString = value.toString();
            if ("object".equalsIgnoreCase(valueString) || "String".equalsIgnoreCase(valueString) || "array".equalsIgnoreCase(valueString)) {
                continue;
            }
            try {
                JSONObject jsonObject = JSONObject.parseObject(valueString);
                String type = jsonObject.getString("type");
                String description = jsonObject.getString("description");
                //设置参数必填项，根据此字段来决定文档中的参数是否需要必填
                String argrequire = isNull ? "否" : "是";
                RowRenderData row0 = Rows.create(prefix + key, type, description, argrequire);
                renderList.addRow(row0);
                //判断是否还有子节点，如果有的话则递归处理余下的子节点，直到没有子节点为止跳出递归
                Object items = jsonObject.get("items");
                if (Objects.nonNull(items) && JSON.parseObject(items.toString()).size() > 1) {
                    JSONObject jsonObjectChild = JSON.parseObject(items.toString());
                    JSONArray required = jsonObjectChild.getJSONArray("required");
                    Object propertiesObj = jsonObjectChild.get("properties");
                    if (Objects.nonNull(required) && required.size() > 0) {
                        List<String> javaList = required.toJavaList(String.class);
                        stringMap = javaList.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
                    }
                    if (Objects.nonNull(propertiesObj)) {
                        //使用递归处理子节点
                        JSONObject propertiesJsonObj = JSON.parseObject(propertiesObj.toString());
                        setFileBody(propertiesJsonObj, renderList, prefix, stringMap);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static String readFile(String path) {
        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(new FileInputStream(path));
            int ch = 0;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static JSONObject cleanData(String data) {
        String cleanedJson = data.replaceAll("//.*", "");
        // 替换所有非标准空格为普通空格
        cleanedJson = cleanedJson.replaceAll("[\\u00A0\\u200B\\uFEFF]", " ");
        // 移除可能的BOM头（UTF-8 BOM）
        cleanedJson = cleanedJson.replace("\uFEFF", "");
        JSONObject object = null;
        try {
            object = JSON.parseObject(cleanedJson);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(data);
            throw new RuntimeException(e.getMessage());
        }
        return object;
    }
}
