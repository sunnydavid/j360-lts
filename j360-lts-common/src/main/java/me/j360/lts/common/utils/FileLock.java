package me.j360.lts.common.utils;


import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * �ļ���
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class FileLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLock.class);
    private File file;

    private FileChannel channel = null;
    private java.nio.channels.FileLock lock = null;

    public FileLock(String filename) {
        this(new File(filename));
    }

    public FileLock(File file) {
        this.file = file;
        FileUtils.createFileIfNotExist(file);
    }

    /**
     * �����
     */
    public boolean tryLock() {
        boolean success = false;
        try {
            Path path = Paths.get(file.getPath());
            if (channel != null && channel.isOpen()) {
                return false;
            }
            channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ);
            lock = channel.tryLock();
            if (lock != null) {
                success = true;
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (!success) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return false;
    }

    /**
     * �ͷ���
     */
    public void release() {
        try {
            if (lock != null) {
                lock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                try {
                    channel.close();   // also releases the lock
                } catch (IOException e) {
                    LOGGER.error("file channel close failed.", e);
                }
            }
        }
    }
}