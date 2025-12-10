package com.bamboo.core.dao.mapper;

import com.bamboo.core.dao.bean.Contract;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractMapper {

    Contract getContract(String contractId);

    void updateContractStatus(String contractId, String status);

}
