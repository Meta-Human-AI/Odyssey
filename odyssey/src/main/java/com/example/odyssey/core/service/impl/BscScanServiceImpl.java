package com.example.odyssey.core.service.impl;

import com.example.odyssey.core.scheduled.BscScanTransactionScheduled;
import com.example.odyssey.core.service.BscScanService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 留个接口，方便以后直接执行定时任务
 */
@Service
@Transactional
public class BscScanServiceImpl implements BscScanService {

}
