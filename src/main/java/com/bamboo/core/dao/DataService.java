package com.bamboo.core.dao;

import com.bamboo.core.dao.mapper.ContractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataService {
    @Autowired
    ContractMapper contractMapper;

    public void updateContractStatus(String contrctId, String status){
        contractMapper.updateContractStatus(contrctId, status);
    }


}
