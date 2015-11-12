package me.j360.lts.biz.logger;


import me.j360.lts.common.extension.SPI;

import java.util.List;

/**
 * ִ��������־��¼��
 *
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
@SPI("console")
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public void log(List<JobLogPo> jobLogPos);

}