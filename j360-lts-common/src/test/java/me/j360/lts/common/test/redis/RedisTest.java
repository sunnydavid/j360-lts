package me.j360.lts.common.test.redis;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/9/15.
 */
public class RedisTest {

    Jedis jedis;

    @Before
    public void connectionTest() {
        jedis = new Jedis("127.0.0.1", 6379);//redis�ĵ�ַ�Լ����Ӷ˿�
        // jedis.auth("helloworld");  //����������֤�������ļ���Ϊ requirepass helloworld����ʱ����Ҫִ�и÷���
    }

    @Test
    public void stringTest() {
        jedis.set("hello", "hello");
        System.out.println(jedis.get("hello"));

// ʹ��append ���ַ����������
        jedis.append("hello", " world");
        System.out.println(jedis.get("hello"));

// set�����ַ���
        jedis.set("hello", "123");
        System.out.println(jedis.get("hello"));

// ���ù���ʱ��
        jedis.setex("hello2", 2, "world2");
        System.out.println(jedis.get("hello2"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        System.out.println(jedis.get("hello2"));

// һ����Ӷ��key-value��
        jedis.mset("a", "1", "b", "2");
// ��ȡa��b��value
        List<String> valus = jedis.mget("a", "b");
        System.out.println(valus);

// ����ɾ��
        jedis.del("a", "b");
        System.out.println(jedis.exists("a"));
        System.out.println(jedis.exists("b"));
    }

    @Test
    public void testHash() {
// �������
        System.out.println(jedis.flushDB());
        String key = "myhash";
        Map<String, String> hash = new HashMap<String, String>();
        hash.put("aaa", "11");
        hash.put("bbb", "22");
        hash.put("ccc", "33");

// �������
        jedis.hmset(key, hash);
        jedis.hset(key, "ddd", "44");

// ��ȡhash������Ԫ��(keyֵ)
        System.out.println(jedis.hkeys(key));

// ��ȡhash�����е�key��Ӧ��valueֵ
        System.out.println(jedis.hvals(key));

// ��ȡhash������Ԫ�ص�����
        System.out.println(jedis.hlen(key));

// ��ȡhash��ȫ�������ֵ,��Map<String, String> ����ʽ����
        Map<String, String> elements = jedis.hgetAll(key);
        System.out.println(elements);

// �жϸ���keyֵ�Ƿ�����ڹ�ϣ����
        System.out.println(jedis.hexists(key, "bbb"));

// ��ȡhash����ָ���ֶζ�Ӧ��ֵ
        System.out.println(jedis.hmget(key, "aaa", "bbb"));

// ��ȡָ����ֵ
        System.out.println(jedis.hget(key, "aaa"));

// ɾ��ָ����ֵ
        System.out.println(jedis.hdel(key, "aaa"));
        System.out.println(jedis.hgetAll(key));

// Ϊkey�е��� field ��ֵ�������� increment
        System.out.println(jedis.hincrBy(key, "bbb", 100));
        System.out.println(jedis.hgetAll(key));
    }

}
