package me.j360.lts.failstore;


import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.utils.FileLock;
import me.j360.lts.common.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 7/5/15.
 */
public abstract class AbstractFailStore implements FailStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailStore.class);

    protected FileLock fileLock;

    private String home;
    protected File dbPath;
    private static final String dbLockName = "___db.lock";

    public AbstractFailStore(File dbPath) {
        this.dbPath = dbPath;
        String path = dbPath.getPath();
        this.home = path.substring(0, path.indexOf(getName())).concat(getName());
        init();
    }

    protected String getLock(String failStorePath) {
        //  get sequence
        FileUtils.createDirIfNotExist(failStorePath);
        // �п�����������ͬʱ�������Ŀ¼���������ļ������õ�����Ȩ
        fileLock = new FileLock(failStorePath.concat("/").concat(dbLockName));
        boolean locked = fileLock.tryLock();    // 1s
        if (!locked) {
            throw new IllegalStateException("can not get current file lock.");
        }
        LOGGER.info("Current failStore path is {}", failStorePath);
        return failStorePath;
    }

    public List<FailStore> getDeadFailStores() {
        File homeDir = new File(home);
        File[] subFiles = homeDir.listFiles();
        List<FailStore> deadFailStores = new ArrayList<FailStore>();
        if (subFiles != null && subFiles.length != 0) {
            for (File subFile : subFiles) {
                try {
                    FileLock tmpLock = new FileLock(subFile.getPath().concat("/").concat(dbLockName));
                    boolean locked = tmpLock.tryLock();
                    if (locked) {
                        // �ܻ������˵�����Ŀ¼����Ӧ�Ľڵ��Ѿ�down��
                        FailStore failStore = getFailStore(subFile);
                        if (failStore != null) {
                            deadFailStores.add(failStore);
                        }
                        tmpLock.release();
                    }
                } catch (Exception e) {
                    // ignore
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return deadFailStores;
    }

    private FailStore getFailStore(File dbPath) {
        try {
            Constructor constructor = this.getClass().getConstructor(File.class);
            return (FailStore) constructor.newInstance(dbPath);
        } catch (Exception e) {
            LOGGER.error("new instance failStore failed,", e);
        }
        return null;
    }

    protected abstract void init();

    protected abstract String getName();

    @Override
    public String getPath() {
        return dbPath.getPath();
    }
}
