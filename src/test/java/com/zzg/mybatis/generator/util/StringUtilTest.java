package com.zzg.mybatis.generator.util;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by Owen on 6/18/16.
 */
public class StringUtilTest {

    public static void main(String[] args) throws Exception{
        File file = new File("./mybatis-generator-gui/src/test/resources/toCamelStyle.txt");
        List<String> lines = FileUtils.readLines(file, "utf-8");

        lines.forEach(StringUtilTest::print);
    }

    public static void print(String str) {
        System.out.println(MyStringUtils.dbStringToCamelStyle2(str));
    }

    @Test
    public void testDbStringToCamelStyle() {
        String result = MyStringUtils.dbStringToCamelStyle("person_address");
        Assert.assertEquals("PersonAddress", result);
    }

    @Test
    public void testDbStringToCamelStyle_case2() {
        String result = MyStringUtils.dbStringToCamelStyle("person_address_name");
        Assert.assertEquals("PersonAddressName", result);
    }

    @Test
    public void testDbStringToCamelStyle_case3() {
        String result = MyStringUtils.dbStringToCamelStyle("person_db_name");
        Assert.assertEquals("PersonDbName", result);
    }

    @Test
    public void testDbStringToCamelStyle_case4() {
        String result = MyStringUtils.dbStringToCamelStyle("person_jobs_");
        Assert.assertEquals("PersonJobs", result);
    }

    @Test
    public void testDbStringToCamelStyle_case5() {
        String result = MyStringUtils.dbStringToCamelStyle("a");
        Assert.assertEquals("A", result);
    }

}
