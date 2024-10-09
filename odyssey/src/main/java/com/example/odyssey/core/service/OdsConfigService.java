package com.example.odyssey.core.service;

import com.example.odyssey.bean.MultiResponse;
import com.example.odyssey.bean.SingleResponse;
import com.example.odyssey.bean.cmd.OdsConfigUpdateCmd;
import com.example.odyssey.bean.dto.OdsConfigDTO;

public interface OdsConfigService {

    SingleResponse update(OdsConfigUpdateCmd odsConfigUpdateCmd);

    MultiResponse<OdsConfigDTO> list();
}
