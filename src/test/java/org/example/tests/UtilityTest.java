package org.example.tests;

import org.example.FtpClient;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.TreeMap;

public class UtilityTest {

    //==================================================================================================================

    @DataProvider(name = "json string")
    public static Object[][] createJsonString() {
        return new Object[][]{{
                "{\"students\":[" +
                        "{\"id\":1,\"name\":\"Ivan\"}," +
                        "{\"id\":4,\"name\":\"Nikolay\"}," +
                        "{\"id\":22,\"name\":\"Andrew\"}" +
                        "]}"
        }};
    }

    @Test(description = "Проверка на преобразование JSON в Map", dataProvider = "json string")
    public void wrongLogin_Test(String json) {
        Map<Integer, String> expected = new TreeMap<>();
        expected.put(1, "Ivan");
        expected.put(4, "Nikolay");
        expected.put(22, "Andrew");

        Assert.assertEquals(FtpClient.parseJsonAndGetMap(json), expected);
    }

    //==================================================================================================================

    @DataProvider(name = "map")
    public static Object[][] createMap() {
        return new Object[][]{{
                new TreeMap<Integer, String>() {{
                    put(1, "Ivan");
                    put(4, "Nikolay");
                    put(22, "Andrew");
                }}
        }};
    }

    @Test(description = "Проверка на преобразование Map в JSON", dataProvider = "map")
    public void wrongPassword_Test(Map<Integer, String> map) {
        String expected = "{\"students\":[{\"id\":1,\"name\":\"Ivan\"},{\"id\":4,\"name\":\"Nikolay\"},{\"id\":22,\"name\":\"Andrew\"}]}";
        Assert.assertEquals(FtpClient.generateStudentJsonString(map), expected);
    }

    //==================================================================================================================
}
