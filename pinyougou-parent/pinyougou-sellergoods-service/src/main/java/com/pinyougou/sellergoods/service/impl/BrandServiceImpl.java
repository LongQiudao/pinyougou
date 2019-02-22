package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;


@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	
	//查询所有品牌
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
	}

	//品牌分页查询
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	//增加品牌
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}

	//加载品牌
	public TbBrand findOne(Long id) {
		
		return brandMapper.selectByPrimaryKey(id);
	}

	//修改品牌
	public void update(TbBrand brand) {
		
		brandMapper.updateByPrimaryKey(brand);
	}

	//批量删除品牌
	public void delete(Long[] ids) {

		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
		
	}

	//品牌组合条件分页查询
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();
		if(brand !=null) {
			if(brand.getName()!=null && brand.getName().length()>0) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0) {
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example );
		return new PageResult(page.getTotal(), page.getResult());
	}

	//返回品牌下拉列表数据
	public List<Map> selectOptionList() {
	
		return brandMapper.selectOptionList();
	}

	

}
