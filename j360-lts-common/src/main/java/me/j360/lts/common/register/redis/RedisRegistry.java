package me.j360.lts.common.register.redis;

import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.constant.Constants;
import me.j360.lts.common.exception.NodeRegistryException;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.register.FailbackRegistry;
import me.j360.lts.common.register.NodeRegistryUtils;
import me.j360.lts.common.register.NotifyEvent;
import me.j360.lts.common.register.NotifyListener;
import me.j360.lts.common.support.Application;
import me.j360.lts.common.support.Config;
import me.j360.lts.common.support.SystemClock;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.utils.NamedThreadFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class RedisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRegistry.class);

    private final Map<String, JedisPool> jedisPools = new ConcurrentHashMap<String, JedisPool>();

    private String clusterName;
    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("LTSRedisRegistryExpireTimer", true));
    private final ScheduledFuture<?> expireFuture;
    private final int expirePeriod;
    private boolean replicate;
    private final int reconnectPeriod;
    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<String, Notifier>();
    private RedisLock lock;

    public RedisRegistry(Application application) {
        super(application);
        Config config = application.getConfig();
        this.clusterName = config.getClusterName();
        this.lock = new RedisLock("LTS_CLEAN_LOCK_KEY", config.getIdentity(), 2 * 60);  // �������ӹ���

        JedisPoolConfig redisConfig = new JedisPoolConfig();
        // TODO ��������n�����
        String address = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());

        String cluster = config.getParameter("cluster", "failover");
        if (!"failover".equals(cluster) && !"replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        replicate = "replicate".equals(cluster);

        this.reconnectPeriod = config.getParameter(Constants.REGISTRY_RECONNECT_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RECONNECT_PERIOD);

        String[] addrs = address.split(",");
        for (String addr : addrs) {
            int i = addr.indexOf(':');
            String host = addr.substring(0, i);
            int port = Integer.parseInt(addr.substring(i + 1));
            this.jedisPools.put(addr, new JedisPool(redisConfig, host, port,
                    Constants.DEFAULT_TIMEOUT));
        }

        this.expirePeriod = config.getParameter(Constants.SESSION_TIMEOUT_KEY, Constants.DEFAULT_SESSION_TIMEOUT);

        this.expireFuture = expireExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    deferExpired(); // �ӳ�����ʱ��
                } catch (Throwable t) { // �������ݴ�
                    LOGGER.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
                }
            }
        }, expirePeriod / 2, expirePeriod / 2, TimeUnit.MILLISECONDS);
    }

    private void deferExpired() {
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    for (Node node : new HashSet<Node>(getRegistered())) {
                        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
                        if (jedis.hset(key, node.toFullString(), String.valueOf(SystemClock.now() + expirePeriod)) == 1) {
                            jedis.publish(key, Constants.REGISTER);
                        }
                    }
                    if (lock.acquire(jedis)) {
                        clean(jedis);
                    }
                    if (!replicate) {
                        break;// ?�������������ͬ�����ݣ�ֻ��д�뵥̨����
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                LOGGER.warn("Failed to write provider heartbeat to redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    private void clean(Jedis jedis) {
        // /LTS/{��Ⱥ����}/NODES/
        Set<String> nodeTypePaths = jedis.keys(NodeRegistryUtils.getRootPath(application.getConfig().getClusterName()) + "/*");
        if (CollectionUtils.isNotEmpty(nodeTypePaths)) {
            for (String nodeTypePath : nodeTypePaths) {
                // /LTS/{��Ⱥ����}/NODES/JOB_TRACKER
                Set<String> nodePaths = jedis.keys(nodeTypePath);
                if (CollectionUtils.isNotEmpty(nodePaths)) {
                    for (String nodePath : nodePaths) {
                        Map<String, String> nodes = jedis.hgetAll(nodePath);
                        if (CollectionUtils.isNotEmpty(nodes)) {
                            boolean delete = false;
                            long now = SystemClock.now();
                            for (Map.Entry<String, String> entry : nodes.entrySet()) {
                                String key = entry.getKey();
                                long expire = Long.parseLong(entry.getValue());
                                if (expire < now) {
                                    jedis.hdel(nodePath, key);
                                    delete = true;
                                    if (LOGGER.isWarnEnabled()) {
                                        LOGGER.warn("Delete expired key: " + nodePath + " -> value: " + entry.getKey() + ", expire: " + new Date(expire) + ", now: " + new Date(now));
                                    }
                                }
                            }
                            if (delete) {
                                jedis.publish(nodePath, Constants.UNREGISTER);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void doRegister(Node node) {
        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
        String expire = String.valueOf(SystemClock.now() + expirePeriod);
        boolean success = false;
        NodeRegistryException exception = null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hset(key, node.toFullString(), expire);
                    jedis.publish(key, Constants.REGISTER);
                    success = true;
                    if (!replicate) {
                        break; // ?�������������ͬ�����ݣ�ֻ��д�뵥̨����
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                exception = new NodeRegistryException("Failed to register node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOGGER.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    protected void doUnRegister(Node node) {
        String key = NodeRegistryUtils.getNodeTypePath(clusterName, node.getNodeType());
        boolean success = false;
        NodeRegistryException exception = null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hdel(key, node.toFullString());
                    jedis.publish(key, Constants.UNREGISTER);
                    success = true;
                    if (!replicate) {
                        break; // ?�������������ͬ�����ݣ�ֻ��д�뵥̨����
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                exception = new NodeRegistryException("Failed to unregister node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOGGER.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {

        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isEmpty(listenNodeTypes)) {
            return;
        }
        for (NodeType listenNodeType : listenNodeTypes) {
            String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);

            Notifier notifier = notifiers.get(listenNodePath);
            if (notifier == null) {
                Notifier newNotifier = new Notifier(listenNodePath);
                notifiers.putIfAbsent(listenNodePath, newNotifier);
                notifier = notifiers.get(listenNodePath);
                if (notifier == newNotifier) {
                    notifier.start();
                }
            }

            boolean success = false;
            NodeRegistryException exception = null;
            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        doNotify(jedis, Collections.singletonList(listenNodePath), Collections.singletonList(listener));
                        success = true;
                        break; // ֻ���һ��������������

                    } finally {
                        jedis.close();
                    }
                } catch (Throwable t) {
                    exception = new NodeRegistryException("Failed to unregister node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
                }
            }
            if (exception != null) {
                if (success) {
                    LOGGER.warn(exception.getMessage(), exception);
                } else {
                    throw exception;
                }
            }

        }
    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            expireFuture.cancel(true);
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        try {
            for (Notifier notifier : notifiers.values()) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            LOGGER.warn(t.getMessage(), t);
        }
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                jedisPool.destroy();
            } catch (Throwable t) {
                LOGGER.warn("Failed to destroy the redis registry client. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    private ConcurrentHashMap<String/*key*/, List<String>> cachedNodeMap = new ConcurrentHashMap<String, List<String>>();

    private void doNotify(Jedis jedis, Collection<String> keys, Collection<NotifyListener> listeners) {
        if (CollectionUtils.isEmpty(keys)
                && CollectionUtils.isEmpty(listeners)) {
            return;
        }

        for (String key : keys) {

            Map<String, String> values = jedis.hgetAll(key);
            List<String> currentChildren = values == null ? new ArrayList<String>(0) : new ArrayList<String>(values.keySet());
            List<String> oldChildren = cachedNodeMap.get(key);

            // 1. �ҳ����ӵ� �ڵ�
            List<String> addChildren = CollectionUtils.getLeftDiff(currentChildren, oldChildren);
            // 2. �ҳ����ٵ� �ڵ�
            List<String> decChildren = CollectionUtils.getLeftDiff(oldChildren, currentChildren);

            if (CollectionUtils.isNotEmpty(addChildren)) {
                List<Node> nodes = new ArrayList<Node>(addChildren.size());
                for (String child : addChildren) {
                    Node node = NodeRegistryUtils.parse(child);
                    nodes.add(node);
                }
                for (NotifyListener listener : listeners) {
                    notify(NotifyEvent.ADD, nodes, listener);
                }
            }
            if (CollectionUtils.isNotEmpty(decChildren)) {
                List<Node> nodes = new ArrayList<Node>(decChildren.size());
                for (String child : decChildren) {
                    Node node = NodeRegistryUtils.parse(child);
                    nodes.add(node);
                }
                for (NotifyListener listener : listeners) {
                    notify(NotifyEvent.REMOVE, nodes, listener);
                }
            }
            cachedNodeMap.put(key, currentChildren);
        }
    }

    private void doNotify(Jedis jedis, String key) {
        for (Map.Entry<Node, Set<NotifyListener>> entry : new HashMap<Node, Set<NotifyListener>>(getSubscribed()).entrySet()) {
            doNotify(jedis, Collections.singletonList(key), new HashSet<NotifyListener>(entry.getValue()));
        }
    }

    private class NotifySub extends JedisPubSub {

        private final JedisPool jedisPool;

        public NotifySub(JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }

        @Override
        public void onMessage(String key, String msg) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals(Constants.REGISTER)
                    || msg.equals(Constants.UNREGISTER)) {
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        doNotify(jedis, key);
                    } finally {
                        jedis.close();
                    }
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }
    }

    // ������߳������redis�Ƿ����
    private volatile String monitorId;
    private volatile boolean redisAvailable = false;

    private class Notifier extends Thread {

        private final String listenNodePath;

        private volatile Jedis jedis;

        private volatile boolean running = true;

        public Notifier(String listenNodePath) {
            super.setDaemon(true);
            super.setName("LTSRedisSubscribe");
            this.listenNodePath = listenNodePath;
            if (monitorId == null) {
                monitorId = listenNodePath;
            }
        }

        @Override
        public void run() {
            try {
                while (running) {
                    int retryTimes = 0;
                    for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                        try {
                            JedisPool jedisPool = entry.getValue();
                            jedis = jedisPool.getResource();
                            if (listenNodePath.equals(monitorId) && !redisAvailable) {
                                redisAvailable = true;
                                application.getRegistryStatMonitor().setAvailable(redisAvailable);
                            }
                            try {
                                retryTimes = 0;
                                jedis.subscribe(new NotifySub(jedisPool), listenNodePath); // ����
                                break;
                            } finally {
                                jedis.close();
                            }
                        } catch (Throwable t) { // ������һ̨
                            LOGGER.warn("Failed to subscribe node from redis registry. registry: " + entry.getKey(), t);
                            if (++retryTimes % jedisPools.size() == 0) {
                                // ���������redis�������ã���Ҫ��Ϣһ�ᣬ�����תռ�ù���cpu��Դ
                                sleep(reconnectPeriod);
                                if (listenNodePath.equals(monitorId) && redisAvailable) {
                                    redisAvailable = false;
                                    application.getRegistryStatMonitor().setAvailable(redisAvailable);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }

        public void shutdown() {
            try {
                running = false;
                jedis.disconnect();
            } catch (Throwable t) {
                LOGGER.warn(t.getMessage(), t);
            }
        }
    }

}
