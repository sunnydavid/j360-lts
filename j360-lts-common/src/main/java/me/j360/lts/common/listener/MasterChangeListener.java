package me.j360.lts.common.listener;

import me.j360.lts.common.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 * Master �ڵ�仯 ������
 */
public interface MasterChangeListener {

    /**
     * master Ϊ master�ڵ�
     * isMaster ��ʾ��ǰ�ڵ��ǲ���master�ڵ�
     * @param master
     * @param isMaster
     */
    public void change(Node master, boolean isMaster);

}
