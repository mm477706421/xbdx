package com.gg.jwglxt;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.interfaces.RSAKey;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class User {
    public User() {
        this.isProxy = false;
    }

    public User(boolean isProxy) {
        this.isProxy = isProxy;
    }

    private final boolean isProxy;
    private final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));

    public String getStuName() {
        return stuName;
    }

    private final Map<String, String> headers = new HashMap<>();
    private long timestamp;
    private String stuName = "";
    private String username;

    public void refreshAccount(String yhm) throws IOException {
        String url = "http://jwgl.nwu.edu.cn/jwglxt/xtgl/login_cxUpdateDlsbcs.html";
        Map<String, String> data = new HashMap<>();
        data.put("yhm", yhm);
        Connection con = Jsoup.connect(url).method(Connection.Method.POST);
        for (Map.Entry<String, String> x : data.entrySet()) {
            con.data(x.getKey(), x.getValue());
        }
        Connection.Response response;
        if (isProxy) {
            response = con.ignoreContentType(true).proxy(proxy).execute();
        } else {
            response = con.ignoreContentType(true).execute();
        }
        headers.put("Host", "jwgl.nwu.edu.cn");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0");
        headers.put("Cookie", response.headers("Set-Cookie").toArray()[0].toString() + response.headers("Set-Cookie").toArray()[1].toString());
//        System.out.println(headers.get("Cookie"));
    }

    public String encode(String password) throws IOException, ScriptException {

        Map<String, String> info = getInfoKey();
        String modulus = info.get("modulus");
        String exponent = info.get("exponent");
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        String rSAPath = Objects.requireNonNull(User.class.getResource("")).getPath();
        engine.eval(new FileReader(rSAPath + "jsbn.js"));
        engine.eval(new FileReader(rSAPath + "prng4.js"));
        engine.eval(new FileReader(rSAPath + "rng.js"));
        engine.eval(new FileReader(rSAPath + "RSA.js"));
        engine.eval(new FileReader(rSAPath + "base64.js"));
        engine.eval("var rsaKey = new RSAKey();");

        String modulesHex = engine.eval("b64tohex(\"" + modulus + "\")").toString();
        String exponentHex = engine.eval("b64tohex(\"" + exponent + "\")").toString();
        engine.eval("rsaKey.setPublic(\"" + modulesHex + "\", \"" + exponentHex + "\");");
        String evalStr = "hex2b64(rsaKey.encrypt(\"" + password + "\"))";
        return engine.eval(evalStr).toString();
    }

    public Map<String, String> getInfoKey() throws IOException {
        Map<String, String> info = new HashMap<>();
        Date date = new Date(System.currentTimeMillis());
        timestamp = date.getTime();
        String url = "http://jwgl.nwu.edu.cn/jwglxt/xtgl/login_getPublicKey.html?time=" + timestamp;
//        System.out.println(url);
        Connection.Response response;
        if (isProxy) {
            response = Jsoup.connect(url).ignoreContentType(true).headers(headers).proxy(proxy).execute();
        } else {
            response = Jsoup.connect(url).ignoreContentType(true).headers(headers).execute();
        }
//        System.out.println(headers.get("Cookie"));
        JSONObject object = JSONObject.parseObject(response.body());
        info.put("modulus", object.getString("modulus"));
        info.put("exponent", object.getString("exponent"));
        return info;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public boolean Login(String username, String password) throws IOException, ScriptException {
        this.username = username;
        try {
            refreshAccount(username);
        } catch (Exception e) {
            System.out.println("用户状态更新失败");
            e.printStackTrace();
        }
        String passwordEncryptoed = encode(password);
        String url = "http://jwgl.nwu.edu.cn/jwglxt/xtgl/login_slogin.html?time=";
//        System.out.println(timestamp);
        Connection con = Jsoup.connect(url + timestamp);
        con.headers(headers);
//        System.out.println(headers.get("Cookie"));
        Connection.Response res;
        if (isProxy) {
            res = Jsoup.connect(url).ignoreContentType(true).headers(headers).proxy(proxy).execute();
        } else {
            res = Jsoup.connect(url).ignoreContentType(true).headers(headers).execute();
        }
        System.out.println(res.headers());
        Document doc = Jsoup.parse(res.body());
        Element elem = doc.getElementById("csrftoken");
        String csrftoken = elem.attr("value");
        Connection login = Jsoup.connect("http://jwgl.nwu.edu.cn/jwglxt/xtgl/login_slogin.html?time=" + timestamp).headers(headers);
//        System.out.println(username);
//        System.out.println(passwordEncryptoed);
        Map<String, String> data = new HashMap<>();
        data.put("mm", passwordEncryptoed);
        data.put("yhm", username);
        data.put("language", "zh_CN");
        data.put("csrftoken", csrftoken);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            login.data(entry.getKey(), entry.getValue());
        }
        headers.replace("Cookie", headers.get("Cookie").split(";")[0] + ";" + headers.get("Cookie").split(";")[2].split("Only")[1]);
//        System.out.println("Test");
        Connection.Response responseLogin;
        if (isProxy) {
            responseLogin = login.method(Connection.Method.POST).followRedirects(true).headers(headers).proxy(proxy).execute();
        } else {
            responseLogin = login.method(Connection.Method.POST).followRedirects(true).headers(headers).execute();
        }

//        System.out.println("Test");

//        System.out.println(responseLogin.headers());
//        System.out.println(headers);
//        System.out.println(responseLogin.body());
//        System.out.println(responseLogin.cookies().get("JSESSIONID"));
        System.out.println(headers.get("Cookie"));
        headers.replace("Cookie", "JSESSIONID=" + responseLogin.cookies().get("JSESSIONID") + ";" + headers.get("Cookie").split(";")[1]);
        Element element = Jsoup.connect("http://jwgl.nwu.edu.cn/jwglxt/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default&su=" + username).headers(headers).execute().parse().getElementsByClass("form-control-static").get(1);
        this.stuName = element.text();
        System.out.println(stuName);
        return responseLogin.statusCode() == 200;
    }

    public String getCourses() throws IOException {
        String url = "http://jwgl.nwu.edu.cn/jwglxt/xsxk/zzxkyzb_cxZzxkYzbPartDisplay.html?gnmkdm=N253512&su=" + username;
        System.out.println(url);
        Map<String, String> coursesheaders = new HashMap<>();
        coursesheaders.put("Host", "jwgl.nwu.edu.cn");
        coursesheaders.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        coursesheaders.put("Cookie", headers.get("Cookie"));
        Connection con = Jsoup.connect(url).headers(coursesheaders).method(Connection.Method.POST);
        System.out.println(headers);
        Map<String, String> data = new HashMap<>();
        data.put("rwlx", "2");
        data.put("xkly", "0");//
        data.put("bklx_id", "0");
        data.put("xz", "4");
        data.put("sfkkjyxdxnxq", "0");
        data.put("xqh_id", "1");
        data.put("jg_id", "19");//
        data.put("njdm_id_1", "2020");
        data.put("zyh_id_1", "1901");//专业id
        data.put("zyh_id", "1901");//专业id
        data.put("zyfx_id", "wfx");//专业方向
        data.put("njdm_id", "2020");//
        data.put("bh_id", "2020190101");//
        data.put("xbm", "1");
        data.put("xslbdm", "421");
        data.put("ccdm", "3");//
        data.put("xsbj", "8");
        data.put("sfkknj", "0");
        data.put("sfkkzy", "0");
        data.put("kzybkxy", "0");
        data.put("sfznkx", "0");
        data.put("zdkxms", "0");
        data.put("sfkxq", "0");
        data.put("sfkcfx", "0");
        data.put("kkbkdj", "0");
        data.put("kkbk", "0");
        data.put("sfkgbcx", "0");
        data.put("sfrxtgkcxd", "0");
        data.put("tykczgxdcs", "0");
        data.put("xkxnm", "2021");//
        data.put("xkxqm", "12");
        data.put("kklxdm", "10");//10选修课 01专业课
        data.put("rlkz", "0");
        data.put("xkzgbj", "0");
        data.put("kspage", "1");
        data.put("jspage", "1000");
        data.put("jxbzb", "");
        for (Map.Entry<String, String> x : data.entrySet()) {
            con.data(x.getKey(), x.getValue());
        }

        Connection.Response response;
        if (isProxy) {
            response = con.proxy(proxy).followRedirects(true).ignoreContentType(true).execute();
        } else {
            response = con.followRedirects(true).ignoreContentType(true).execute();
        }
        return response.body();
//        System.out.println(url);
    }

    public static void main(String[] args) throws IOException, ScriptException {
        User user1 = new User();
        user1.Login("2020111111", "Ffk20020215");

        user1.getCourses();
    }
}
