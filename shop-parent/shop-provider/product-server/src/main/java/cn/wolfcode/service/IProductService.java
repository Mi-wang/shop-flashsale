package cn.wolfcode.service;

import cn.wolfcode.domain.Product;

import java.util.List;


public interface IProductService {

    Product getById(Long id);

    List<Product> queryByIdList(List<Long> idList);
}
