package com.roy.github.learn.javabase.util;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;

/**
 *
 */
public class IOUtilEx {

    /**
     * 从输入流中拷贝文件到本地流中
     *
     * @param in
     *            输入流
     * @param out
     *            输出流
     * @return 返回文件hash
     * @throws Exception
     */
    public static String copyBytes(InputStream in, OutputStream out)
            throws Exception {

        if (null == in || null == out) {
            return "";
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        PrintStream ps = out instanceof PrintStream ? (PrintStream) out : null;
        byte buf[] = new byte[2048];
        int bytesRead = in.read(buf);

        while (bytesRead >= 0) {
            out.write(buf, 0, bytesRead);
            md.update(buf, 0, bytesRead);
            if ((ps != null) && ps.checkError()) {
                throw new IOException("Unable to write to output stream.");
            }
            bytesRead = in.read(buf);
        }

        out.close();
        in.close();

        return new String(Hex.encodeHex(md.digest()));
    }

}
