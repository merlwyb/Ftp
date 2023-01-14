package org.example;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FtpClient {
    static private String server;
    final static private int PORT = 21;
    static private String username;
    static private String password;
    static private String ftpStr;
    static final private String filename = "Students.json";

    static private int id = 1;


    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter username");
        username = scan.next();
        System.out.println("Enter password");
        password = scan.next();
        System.out.println("Enter ip-address");
        server = scan.next();


        try {
            checkConnection(username, password, server, PORT);
            System.out.println("Successful login");
        } catch (SocketException | UnknownHostException e) {
            System.out.println("Invalid ip-address");
            e.printStackTrace();
            scan.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Invalid username or password");
            e.printStackTrace();
            scan.close();
            System.exit(0);
        }


        try {
            printOptions();
            String option = scan.next();
            while (!Objects.equals(option, "5")) {
                switch (option) {
                    case "1":
                        printStudentList();
                        break;
                    case "2":
                        System.out.println("Enter ID to display the information");
                        printStudentById(scan.nextInt());
                        break;
                    case "3":
                        System.out.println("Enter a name to add:");
                        addStudent(scan.next());
                        break;
                    case "4":
                        System.out.println("Enter ID to delete the student:");
                        removeStudentById(scan.nextInt());
                        break;
                    default:
                        System.out.println("No such option, try again");
                }
                printOptions();
                option = scan.next();
            }
        } catch (InputMismatchException e) {
            System.out.println("The entered value is not a number: " + e);
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("Failed to connect to the server: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error with the file: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Shutdown...");
            scan.close();
        }
    }


    static void printStudentList() throws IOException {
        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        System.out.println("Students list:");
        studentMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach((ent) ->
                        System.out.println(ent.getKey() + " - " + ent.getValue())
                );
        generateStudentJsonString(studentMap);
    }

    static void printStudentById(int id) throws IOException {
        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        System.out.println("Student with id=" + id + ": " + studentMap.getOrDefault(id, "NOT EXISTS"));
    }

    static void addStudent(String name) throws IOException {
        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        while (studentMap.containsKey(id)) {
            id++;
        }

        studentMap.put(id, name);
        saveMapToFileAndSend(studentMap, ftpStr);

        System.out.println("Student " + name + " was successfully added with id=" + id);

    }

    static void removeStudentById(int id) throws IOException {

        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        studentMap.remove(id);
        saveMapToFileAndSend(studentMap, ftpStr);

        System.out.println("Student with id=" + id + " was successfully deleted");

    }


    // ===================================================================================================================
    // =Utils
    // ===================================================================================================================


    public static void checkConnection(String username, String password, String ip, int p) throws IOException {
        ftpStr = String.format("ftp://%s:%s@%s:%d", username, password, ip, p);
        URL ftpUrl = new URL(ftpStr);
        URLConnection urlConnection = ftpUrl.openConnection();
        urlConnection.connect();
        ftpStr += "/" + filename;
    }

    static void printOptions() {
        System.out.println("1. Getting a list of students");
        System.out.println("2. Getting information about a student by id");
        System.out.println("3. Adding a student");
        System.out.println("4. Deleting a student by id");
        System.out.println("5. Shutdown");
    }

    public static Map<Integer, String> getFileThenMap(String ftpString, String filename) throws IOException {
        URL ftpUrl = new URL(ftpString);
        URLConnection urlConnection = ftpUrl.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        Files.copy(inputStream, new File(filename).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        inputStream.close();
        return parseJsonAndGetMap(content);
    }

    public static void saveMapToFileAndSend(Map<Integer, String> studentMap, String ftpString) throws IOException {
        URL urlAdd = new URL(ftpString);
        URLConnection connAdd = urlAdd.openConnection();
        OutputStream outputStream = connAdd.getOutputStream();
        InputStream inputStream = new ByteArrayInputStream(
                generateStudentJsonString(studentMap).getBytes(StandardCharsets.UTF_8));

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }

    public static Map<Integer, String> parseJsonAndGetMap(String json) {
        Pattern idPattern = Pattern.compile("\"id\" *: *([^,]*),");
        Pattern namePattern = Pattern.compile("\"name\" *: *\"([^,]*)\"");

        Matcher id_matcher = idPattern.matcher(json);
        Matcher name_matcher = namePattern.matcher(json);

        Map<Integer, String> studentsMap = new TreeMap<>();
        while (id_matcher.find() && name_matcher.find()) {
            studentsMap.put(Integer.valueOf(id_matcher.group(1)), name_matcher.group(1));
        }
        return studentsMap;
    }

    public static String generateStudentJsonString(Map<Integer, String> studentMap) {
        StringBuilder stringBuilder = new StringBuilder();
        String s = studentMap.entrySet()
                .stream()
                .map(e -> "{\"id\":" + e.getKey() + ","
                        + "\"name\":" + "\"" + e.getValue() + "\"}")
                .collect(Collectors.joining(","));

        stringBuilder.append("{\"students\":[").append(s).append("]}");
        return stringBuilder.toString();
    }
}
