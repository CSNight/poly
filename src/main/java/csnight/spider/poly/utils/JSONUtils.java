package csnight.spider.poly.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    /**
     * 将一个实体类对象转化成JSON数据格式,等效于object2json
     *
     * @param obj 实体类对象
     * @return JSON数据格式字符串
     */
    public static String pojo2json(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    public static Object map2pojo(Map map, Class javaBean) {
        String str = map2json(map);
        return json2pojo(str, javaBean);
    }

    /**
     * 将数组集合等对象转换成JSON字符串
     *
     * @param list array
     * @return String json
     */
    public static String object2json(Object list) {
        return JSONObject.toJSONString(list);
    }

    /**
     * 将Map准换为JSON字符串,等效于object2json()
     *
     * @param map map集合
     * @return JSON字符串
     */
    public static String map2json(Map<?, ?> map) {
        return JSONObject.toJSONString(map);
    }


    /**
     * 将Json格式的字符串转换成指定的对象返回
     *
     * @param jsonStr  要转化的Json格式的字符串
     * @param javaBean 指定转化对象类型
     * @return 转化后的对象
     */
    public static <T> T json2pojo(String jsonStr, Class<T> javaBean) {
        return JSONObject.parseObject(jsonStr, javaBean);
    }

    /**
     * 将Json格式的字符串转换成Map对象
     *
     * @param jsonString JSON数据格式字符串
     * @return map集合
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> json2Map(String jsonString) {
        return (Map<String, String>) json2pojo(jsonString, Map.class);
    }


    /**
     * 将Json格式的字符串转换成指定对象组成的List返回
     *
     * @param jsonString JSON数据格式字符串
     * @param pojoClass  指定转化对象类型
     * @return list集合
     */
    public static List<?> jsonArray2List(String jsonString,
                                         @SuppressWarnings("rawtypes") Class pojoClass) {
        return JSONArray.parseArray(jsonString, pojoClass);
    }

    public static String ConvertStream2Json(InputStream inputStream) {
        String jsonStr = "";
        // ByteArrayOutputStream相当于内存输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // 将输入流转移到内存输出流中
        try {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            // 将内存流转换为字符串
            jsonStr = new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }
}
