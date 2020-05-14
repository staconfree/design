package com.roy.github.learn.javabase.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩解压缩
 */
public class ZipUtil {

    /**
     * 缓冲区大小
     */
    public final static int BUFFER = 2048;

    /**
     * 压缩文件
     *
     * @param source 待压缩的文件
     * @param target 压缩之后的文件
     * @throws Exception
     */
    public static void zipFile(String source, String target) throws Exception {

        FileInputStream fin = null;
        FileOutputStream fout = null;
        GZIPOutputStream gzout = null;
        byte[] buf = null;

        try {
            // 从源文件得到文件输入流
            fin = new FileInputStream(source);

            // 得到目标文件输出流
            fout = new FileOutputStream(target);

            // 得到压缩输出流
            gzout = new GZIPOutputStream(fout);

            buf = new byte[BUFFER];

            int num = 0;
            while ((num = fin.read(buf)) != -1) {
                gzout.write(buf, 0, num);
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            if (null != gzout) {
                gzout.close();
            }
            if (null != fout) {
                fout.close();
            }
            if (null != fin) {
                fin.close();
            }

            if (null != buf) {
                buf = null;
            }
        }
    }

    /**
     * 解压文件,同时删除压缩文件
     *
     * @param source
     * @param target
     * @throws Exception
     */
    public static void unZipFile(String source, String target) throws Exception {

        FileInputStream fin = null;
        GZIPInputStream gzin = null;
        FileOutputStream fout = null;
        byte[] buf = null;

        try {
            // 得以文件输入流
            fin = new FileInputStream(source);

            // 得到压缩输入流
            gzin = new GZIPInputStream(fin);

            // 得到文件输出流
            fout = new FileOutputStream(target);

            buf = new byte[BUFFER];

            int num = 0;
            while ((num = gzin.read(buf, 0, buf.length)) != -1) {
                fout.write(buf, 0, num);
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            if (null != gzin) {
                gzin.close();
            }
            if (null != fout) {
                fout.close();
            }
            if (null != fin) {
                fin.close();
            }

            if (null != buf) {
                buf = null;
            }
        }
    }

    /**
     * 删除临时产生的文件
     *
     * @param filePath
     * @throws Exception
     */
    public static void deleteTmpFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
        } else {
            // throw new Exception("Error deleteTmpFile,filePath:" + filePath);
        }
    }

}
