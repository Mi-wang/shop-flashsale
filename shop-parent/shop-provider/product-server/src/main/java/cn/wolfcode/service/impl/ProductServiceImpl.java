package cn.wolfcode.service.impl;

import cn.wolfcode.mapper.ProductMapper;
import cn.wolfcode.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

}
