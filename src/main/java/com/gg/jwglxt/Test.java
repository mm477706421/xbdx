package com.gg.jwglxt;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Test {
    public static void main(String[] args) throws IOException {
        String jsonFilePath = Objects.requireNonNull(Test.class.getResource("")).getPath() + "classes.json";
        File file = new File(jsonFilePath);
        String input = FileUtils.readFileToString(file,"gbk");

        JSONObject jsonObject = JSONObject.parseObject(input);
        jsonObject.getJSONArray("tmpList").forEach(System.out::println);

    }
}
