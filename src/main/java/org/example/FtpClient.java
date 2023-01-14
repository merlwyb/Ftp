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
        System.out.println("Введите логин");
        username = scan.next();
        System.out.println("Введите пароль");
        password = scan.next();
        System.out.println("Введите ip-address");
        server = scan.next();


        try {
            checkConnection(username, password, server, PORT);
            System.out.println("Успешный логин");
        } catch (SocketException | UnknownHostException e) {
            System.out.println("Неверный адрес сервера");
            e.printStackTrace();
            scan.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Неверный логин или пароль");
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
                        System.out.println("Введите желаемый id для вывода информации");
                        printStudentById(scan.nextInt());
                        break;
                    case "3":
                        System.out.println("Введите имя для добавления:");
                        addStudent(scan.next());
                        break;
                    case "4":
                        System.out.println("Введите id для удаления студента:");
                        removeStudentById(scan.nextInt());
                        break;
                    default:
                        System.out.println("Нет такого варианта, попробуйте ещё раз");
                }
                printOptions();
                option = scan.next();
            }
        } catch (InputMismatchException e) {
            System.out.println("Введёное значение не является числом: " + e);
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println("Не удалось подключиться к серверу: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Ошибка с файлом: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Завершение работы...");
            scan.close();
        }
    }


    static void printStudentList() throws IOException {
        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        System.out.println("Список студентов:");
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

        System.out.println("Студент с id=" + id + ": " + studentMap.getOrDefault(id, "Не существует"));
    }

    static void addStudent(String name) throws IOException {
        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        while (studentMap.containsKey(id)) {
            id++;
        }

        studentMap.put(id, name);
        saveMapToFileAndSend(studentMap, ftpStr);

        System.out.println("Студент " + name + " был успешно добавлен с id=" + id);

    }

    static void removeStudentById(int id) throws IOException {

        Map<Integer, String> studentMap = getFileThenMap(ftpStr, filename);

        studentMap.remove(id);
        saveMapToFileAndSend(studentMap, ftpStr);

        System.out.println("Студент с id=" + id + " был успешно удалён");

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
        System.out.println("\nВыберите действие:");
        System.out.println("1. Получение списка студентов по имени");
        System.out.println("2. Получение информации о студенте по id");
        System.out.println("3. Добавление студента");
        System.out.println("4. Удаление студента по id");
        System.out.println("5. Завершение работы");
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
