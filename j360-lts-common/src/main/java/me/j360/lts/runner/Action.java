package me.j360.lts.runner;

/**
 * @author Robert HG (254963746@qq.com) on 6/13/15.
 */
public enum Action {

    EXECUTE_SUCCESS,    // ִ�гɹ�,������� ֱ�ӷ����ͻ���
    EXECUTE_FAILED,     // ִ��ʧ��,�������,ֱ�ӷ������ͻ���,������ִ��
    EXECUTE_LATER,       // �Ժ�����ִ��,�������, �������ͻ���,�Ժ�����ִ��,������������Դ���
    EXECUTE_EXCEPTION   // ִ���쳣, �������Ҳ������

}
