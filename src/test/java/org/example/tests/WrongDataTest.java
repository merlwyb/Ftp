package org.example.tests;

import org.example.FtpClient;
import org.testng.annotations.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.InputMismatchException;

import static org.example.config.Config.*;

public class WrongDataTest {

    //==================================================================================================================

    @DataProvider(name = "wrong login")
    public static Object[][] createDataWithWrongLogin() {
        return new Object[][]{
                {"ftp1", PASSWORD, SERVER, PORT},
                {" ", PASSWORD, SERVER, PORT}
        };
    }

    @Test(description = "Checking for an incorrectly entered username",
            expectedExceptions = {IOException.class, IllegalArgumentException.class},
            dataProvider = "wrong login")
    public void wrongLogin_Test(String username, String password, String ip, int p) throws Exception {
        FtpClient.checkConnection(username, password, ip, p);
    }

    //==================================================================================================================

    @DataProvider(name = "wrong password")
    public static Object[][] createDataWithWrongPassword() {
        return new Object[][]{
                {USERNAME, "ftp1", SERVER, PORT},
                {USERNAME, " ", SERVER, PORT}
        };
    }

    @Test(description = "Checking for an incorrectly entered password",
            expectedExceptions = IOException.class,
            dataProvider = "wrong password")
    public void wrongPassword_Test(String username, String password, String ip, int p) throws Exception {
        FtpClient.checkConnection(username, password, ip, p);
    }

    //==================================================================================================================

    @DataProvider(name = "wrong server")
    public static Object[][] createDataWithWrongServer() {
        return new Object[][]{
                {USERNAME, PASSWORD, "1.1.1.1", PORT},
                {USERNAME, PASSWORD, "asd", PORT}
        };
    }

    @Test(description = "Checking for an incorrectly entered ip-address",
            expectedExceptions = {SocketException.class, UnknownHostException.class},
            dataProvider = "wrong server")
    public void wrongServer_Test(String username, String password, String ip, int p) throws Exception {
        FtpClient.checkConnection(username, password, ip, p);
    }

    //==================================================================================================================

}
