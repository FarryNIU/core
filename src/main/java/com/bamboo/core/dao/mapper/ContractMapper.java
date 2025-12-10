package com.bamboo.core.dao.mapper;

import com.bamboo.core.dao.bean.Contract;

public interface ContractMapper {

    Contract getContract(String contractId);

    void addContract(Contract contract);
}
