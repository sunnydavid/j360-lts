package me.j360.lts.common.utils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class FileUtils {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFileIfNotExist(File file) {
        if (!file.exists()) {
            // ������Ŀ¼
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("create file[" + file.getAbsolutePath() + "] failed!", e);
            }
        }
        return file;
    }

    public static File createFileIfNotExist(String path) {
        return createFileIfNotExist(new File(path));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createDirIfNotExist(File file) {
        if (!file.exists()) {
            // ������Ŀ¼
            file.getParentFile().mkdirs();
            file.mkdir();
        }
        return file;
    }

    public static File createDirIfNotExist(String path) {
        return createDirIfNotExist(new File(path));
    }

    public static String read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder createTableSql = new StringBuilder();
        String data = null;
        while ((data = br.readLine()) != null) {
            createTableSql.append(data);
        }
        return createTableSql.toString();
    }

    public static String read(InputStream is, String encoding) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        StringBuilder createTableSql = new StringBuilder();
        String data = null;
        while ((data = br.readLine()) != null) {
            createTableSql.append(data);
        }
        return createTableSql.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            //��Ŀ¼��û���ļ���ֱ��ɾ��
            File[] delFiles = file.listFiles();
            if (delFiles == null || delFiles.length == 0) {
                file.delete();
            } else {
                for (File delFile : delFiles) {
                    if (delFile.isDirectory()) {
                        delete(delFile);
                    }
                    delFile.delete();
                }
            }
        }
        // �Լ�Ҳɾ��
        file.delete();
    }

    public static void delete(String path) {
        delete(new File(path));
    }

    public static void write(CharSequence charSequence, File file, Charset charset, boolean append) {
        Writer writer = null;
        try {
            createFileIfNotExist(file);
            writer = new OutputStreamWriter(new FileOutputStream(file, append), charset);
            writer.append(charSequence);
            writer.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * �õ��ļ������ļ��еĴ�С(�����������ļ�)
     */
    public static long getSize(File file) {
        if (file.exists()) {
            if (!file.isFile()) {
                long size = 0;
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        size += getSize(f);
                    }
                }
                return size;
            } else {
                return file.length();
            }
        }
        return 0;
    }
}
