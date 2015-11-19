package me.j360.lts.common.protocol;

import me.j360.lts.common.support.SystemClock;
import me.j360.lts.remote.RemotingCommandBody;
import me.j360.lts.remote.annotation.NotNull;
import me.j360.lts.remote.annotation.Nullable;
import me.j360.lts.remote.exception.RemotingCommandFieldCheckException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * �����header ������Ϣ
 */
public class AbstractRemotingCommandBody implements RemotingCommandBody {

    /**
     * �ڵ��� ��ǰ�ڵ�� group(ͳһ����, ������ͬ���ܵĽڵ�group��ͬ)
     */
    @NotNull
    private String nodeGroup;

    /**
     * NodeType ���ַ�����ʾ, �ڵ�����
     */
    @NotNull
    private String nodeType;

    /**
     * ��ǰ�ڵ��Ψһ��ʶ
     */
    @NotNull
    private String identity;

    private Long timestamp = SystemClock.now();

    // ����Ĳ���
    @Nullable
    private Map<String, Object> extParams;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public Map<String, Object> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, Object> extParams) {
        this.extParams = extParams;
    }

    public void putExtParam(String key, Object obj) {
        if (this.extParams == null) {
            this.extParams = new HashMap<String, Object>();
        }
        this.extParams.put(key, obj);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {

    }
}
