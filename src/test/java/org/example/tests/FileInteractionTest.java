package org.example.tests;

import org.example.FtpClient;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import static org.example.config.Config.*;

public class FileInteractionTest {
    static private String ftpStr;

    @BeforeTest
    public void setUp() {
        ftpStr = String.format("ftp://%s:%s@%s:%d/%s", USERNAME, PASSWORD, SERVER, PORT, TESTFILENAME);
    }

    //==================================================================================================================

    @Test(description = "Проверка на успешное получение файла")
    public void getFile_Test() throws IOException {
        FtpClient.getFileThenMap(ftpStr, TESTFILENAME);
        Files.delete(Paths.get(TESTFILENAME));
    }

    //==================================================================================================================

    @Test(description = "Проверка на успешную загрузку файла")
    public void saveFile_Test() throws IOException {
        Map<Integer, String> testMap = new TreeMap<>();
        testMap.put(1, "Ivan");
        testMap.put(22, "Andrew");
        testMap.put(4, "Nikolay");
        FtpClient.saveMapToFileAndSend(testMap, ftpStr);
    }
}
