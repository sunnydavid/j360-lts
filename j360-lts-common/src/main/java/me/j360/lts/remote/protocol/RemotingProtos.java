package me.j360.lts.remote.protocol;

public final class RemotingProtos {
    private RemotingProtos() {
    }

    public enum ResponseCode {
        // �ɹ�
        SUCCESS(0),
        // ������δ�����쳣
        SYSTEM_ERROR(1),
        // �����̳߳�ӵ�£�ϵͳ��æ
        SYSTEM_BUSY(2),
        // ������벻֧��
        REQUEST_CODE_NOT_SUPPORTED(3),
        // �����������
        REQUEST_PARAM_ERROR(4);

        private int code;

        ResponseCode(int code) {
            this.code = code;
        }

        public static ResponseCode valueOf(int code) {
            for (ResponseCode responseCode : ResponseCode.values()) {
                if (responseCode.code == code) {
                    return responseCode;
                }
            }
            throw new IllegalArgumentException("can't find the response code !");
        }

        public int code() {
            return this.code;
        }


    }
}
