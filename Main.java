import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Downloader {
    private static final Map<String, byte[]> FILE_SIGNATURES = new HashMap<>();

    static {
        // Определяем сигнатуры файлов
        FILE_SIGNATURES.put("mp3", new byte[]{(byte) 0xFF, (byte) 0xFB});
        FILE_SIGNATURES.put("mp3_ID3", new byte[]{(byte) 0x49, (byte) 0x44, (byte) 0x33}); // ID3
        FILE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);

        System.out.print("Адрес ссылки для загрузки музыки (mp3): ");
        String musicDownloadUrl = inputScanner.nextLine();
        System.out.print("Укажите имя для сохранения музыкального файла: ");
        String musicFileName = inputScanner.nextLine();

        System.out.print("Адрес ссылки для загрузки изображения (jpg): ");
        String imageDownloadUrl = inputScanner.nextLine();
        System.out.print("Укажите имя для сохранения изображения: ");
        String imageFileName = inputScanner.nextLine();

        // Запускаем потоки для загрузки файлов параллельно
        new Thread(() -> downloadContent(musicDownloadUrl, musicFileName)).start();
        new Thread(() -> downloadContent(imageDownloadUrl, imageFileName)).start();
    }

    private static void launchFile(String filePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", filePath);
            processBuilder.start();
        } catch (IOException ex) {
            System.out.println("Не удалось открыть файл: " + ex.getMessage());
        }
    }

    public static void downloadContent(String downloadUrl, String outputFileName) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            try (InputStream inputStream = urlConnection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] headerBytes = new byte[4];  // Считываем первые байты для проверки сигнатуры
                inputStream.read(headerBytes);

                if (!isFileValid(headerBytes, outputFileName)) {
                    System.out.println("Ошибка: файл " + outputFileName + " имеет неправильный тип.");
                    return;
                }

                byteArrayOutputStream.write(headerBytes);  // Записываем проверенные байты
                byteArrayOutputStream.write(inputStream.readAllBytes());  // Записываем оставшиеся байты

                try (OutputStream fileOutputStream = new FileOutputStream(outputFileName)) {
                    byteArrayOutputStream.writeTo(fileOutputStream);  // Сохраняем контент на диск
                }

                launchFile(outputFileName);  // Открываем файл
            }
        } catch (IOException ex) {
            System.out.println("Ошибка при загрузке файла: " + ex.getMessage());
        }
    }

    private static boolean isFileValid(byte[] headerBytes, String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        byte[] expectedBytes = FILE_SIGNATURES.get(fileExtension);

        if (expectedBytes == null) {
            System.out.println("Неизвестное расширение: " + fileExtension);
            return false;
        }

        // Проверяем соответствие сигнатуры (первые байты файла)
        for (int index = 0; index < expectedBytes.length; index++) {
            if (headerBytes[index] != expectedBytes[index]) {
                return false;
            }
        }
        return true;
    }
}
