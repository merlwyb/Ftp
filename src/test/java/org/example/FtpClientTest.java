package org.example;

import org.example.config.Config;
import org.testng.TestNG;
import org.testng.collections.Lists;

import java.util.List;

public class FtpClientTest {
    public static void main(String[] args) {
        TestNG testNG = new TestNG();
        String suiteFileName = args[0];
        Config.USERNAME = args[1];
        Config.PASSWORD = args[2];
        Config.SERVER = args[3];
        Config.TESTFILENAME = args[4];
//        String suiteFileName = "src/test/java/resources/testing.xml";
//        Config.USERNAME = "ftp";
//        Config.PASSWORD = "ftp";
//        Config.SERVER = "192.168.0.105";
//        Config.TESTFILENAME = "Test.json";

        List<String> suites = Lists.newArrayList(suiteFileName);
        testNG.setTestSuites(suites);
        System.out.println("Running tests...");
        testNG.run();
        System.out.println("Tests completed...");
    }
}