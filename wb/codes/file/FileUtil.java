package com.roy.github.learn.javabase.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class FileUtil {

    /**
     * zip文件后缀名
     */
    public final static String ZIP_SUFFIX = ".gz";

    public static List<String> split(String filePath, long chunkSize) {
        String nanoTime = String.format("%05d%2d",System.nanoTime()%100000,new Random().nextInt(100));
        long fileSize = getFileSize(filePath);
        int chunks = getChunks(fileSize, chunkSize);
        if (chunks == 1) {
            chunkSize = fileSize;
        }

        long writeSize = 0;
        long writeTotal = 0;
        List<String> chunkedFilePathList = new ArrayList<>();
        String chunkedFilePath = null;
        for (int i = 1; i <= chunks; i++) {
            if (i < chunks) {
                writeSize = chunkSize;
            } else {
                writeSize = fileSize - writeTotal;
            }
            if (chunks == 1) {
                chunkedFilePath = filePath+"." + nanoTime + ".bak";
            } else {
                chunkedFilePath = filePath+"." + nanoTime + ".part" + i;
            }

            if (!writeChunkedFile(filePath, chunkedFilePath, writeSize, writeTotal)) {
                return null;
            }
            writeTotal = writeTotal + writeSize;
            chunkedFilePathList.add(chunkedFilePath);
        }
        return chunkedFilePathList;
    }

    public static boolean combine(String destFilePath, String[][] chunkedFileInfoArray) {
        RandomAccessFile randomAccessFile = null;
        long writtenByte = 0;
        FileInputStream fileInputStream = null;
        int length = 0;
        byte[] bytes = new byte[1024];
        try {
            randomAccessFile = new RandomAccessFile(destFilePath, "rw");
            for (int i = 0; i < chunkedFileInfoArray.length; i++) {
                randomAccessFile.seek(writtenByte);
                fileInputStream = new FileInputStream(chunkedFileInfoArray[i][0]);
                while ((length = fileInputStream.read(bytes)) > 0) {
                    randomAccessFile.write(bytes, 0, length);
                }
                fileInputStream.close();
                writtenByte += Long.parseLong(chunkedFileInfoArray[i][1]);
            }
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return -1;
        }
        return file.length();
    }

    public static int getChunks(long fileSize, long chunkSize) {
        if (fileSize <= chunkSize) {
            return 1;
        } else {
            if (fileSize % chunkSize != 0) {
                return (int) (fileSize / chunkSize) + 1;
            } else {
                return (int) (fileSize / chunkSize);
            }
        }
    }

    public static boolean writeChunkedFile(String filePath, String chunkedFilePath, long chunkSize, long offset) {
        RandomAccessFile randomAccessFile = null;
        FileOutputStream fileOutputStream = null;
        byte[] bytes = new byte[1024];
        long writtenBytes = 0;
        int length = 0;
        try {
            randomAccessFile = new RandomAccessFile(filePath, "r");
            randomAccessFile.seek(offset);
            fileOutputStream = new FileOutputStream(chunkedFilePath);
            while ((length = randomAccessFile.read(bytes)) > 0) {
                if (writtenBytes < chunkSize) {
                    writtenBytes += length;
                    if (writtenBytes <= chunkSize) {
                        fileOutputStream.write(bytes, 0, length);
                    } else {
                        length -= ((int) (writtenBytes - chunkSize));
                    }
                }
            }

            fileOutputStream.close();
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        String sourceFilePath = "D:\\chunky\\Big.exe";
        List<String> chunkedFilePathList = FileUtil.split(sourceFilePath, 1024 * 1024 * 50);
        for (String chunkedFilePath : chunkedFilePathList) {
            System.out.println("Zipping " + chunkedFilePath);
            String zipFilePath = chunkedFilePath + ZIP_SUFFIX;
            ZipUtil.zipFile(chunkedFilePath, zipFilePath);
            File chunkedFile = new File(chunkedFilePath);
            System.out.println(chunkedFile.getName());
            chunkedFile.delete();

            System.out.println("Unzipping " + zipFilePath);
            String unzipFilePath = chunkedFilePath;
            ZipUtil.unZipFile(zipFilePath, unzipFilePath);
            File zipFile = new File(zipFilePath);
            zipFile.delete();
        }

        String[][] arr = new String[chunkedFilePathList.size()][2];
        for (int i = 0; i < chunkedFilePathList.size(); i++) {
            long size = getFileSize(chunkedFilePathList.get(i));
            arr[i][0] = chunkedFilePathList.get(i);
            arr[i][1] = String.valueOf(size);
        }

        String destFilePath = "D:\\chunky\\Big_copy.exe";
        combine(destFilePath, arr);
    }
}
