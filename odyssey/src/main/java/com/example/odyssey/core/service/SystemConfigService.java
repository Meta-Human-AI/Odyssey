package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.dto.SystemConfigDTO;

public interface SystemConfigService {

    MultiResponse<SystemConfigDTO> listSystemConfig();
}
