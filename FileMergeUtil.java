package roy.github.learn.springall.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * <dependency>
 *             <groupId>commons-io</groupId>
 *             <artifactId>commons-io</artifactId>
 *             <version>2.4</version>
 *         </dependency>
 * 合并文件
 */
public class FileMergeUtil {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FileMergeUtil.class);

    private static final int retryInterval = 5000;
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;
    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;
    /**
     * The file copy buffer size (30 MB)
     */
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    /**
     * 合并给定目录下的所有文件，新文件名为resource的文件名，目录为resource.yak
     *
     * @param r .yak目录对应的resource
     * @throws IOException
     */
    public static void merge(Resource r, String newFileName) throws IOException {

        File f = r.getFile();

        while (!f.exists()) {
            try {
                logger.debug("File {} not exists ,waiting for {} seconds", f.getName(), retryInterval / 1000);
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                logger.warn("Interrupted ", e);
            }
        }

        if (f.isDirectory()) {
            String targetFileName = r.getFile().getParent() + File.separator + newFileName;
            String tmpFileName = r.getFile().getParent()
                    + "/" + "tmp_" + r.getFilename();
            File merge = new File(tmpFileName);
            File target = new File(targetFileName);


            FileOutputStream os = new FileOutputStream(merge);
            FileInputStream is = null;
            FileChannel mergeChannel = null;
            FileChannel fromChannel = null;
            try {
                mergeChannel = os.getChannel();
                File[] files = f.listFiles();
                for (File t : files) {
                    logger.info("merging {}", t.getName());
                    is = new FileInputStream(t);
                    fromChannel = is.getChannel();
                    //                    channel.transferTo(0, channel.size(), mergeChannel);
                    //                    channel.close();
                    transfer(fromChannel, mergeChannel);
                }
            } finally {
                IOUtils.closeQuietly(mergeChannel);
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(fromChannel);
                IOUtils.closeQuietly(is);
            }

            if (target.exists()) {
                logger.warn("File {} exists ,delete it", targetFileName);
                target.delete();
            }
            boolean flag = merge.renameTo(target);
            if (!flag) {
                logger.error("文件最终命名失败!", new IOException("文件最终命名失败!"));
                throw new IOException("文件最终命名失败!");
            }
        }
    }

    private static void transfer(FileChannel from, FileChannel to) throws IOException {
        long size = from.size();
        long pos = 0;
        long count = 0;
        while (pos < size) {
            count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
            pos += from.transferTo(pos, count, to);
        }
    }

    public static void main(String[] args) throws Exception {
        Resource r = new FileSystemResource("d:/temp/abc.yak");
        merge(r, "abc");
    }
}
