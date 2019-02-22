package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;



/**
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {
	
	//查询所有品牌
	public List<TbBrand> findAll();
	
	//品牌分页查询
	public PageResult findPage(int pageNum,int pageSize); 
	
	//品牌增加
	public void add(TbBrand brand);
	
	//加载品牌
	public TbBrand findOne(Long id);
	
	//修改品牌
	public void update(TbBrand brand);
	
	//批量删除品牌
	public void delete(Long[] ids);
	
	//品牌分页组合条件查询
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	//返回下拉列表数据
	public  List<Map> selectOptionList();
}
