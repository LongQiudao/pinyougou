package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	//查询所有品牌
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();		
	}
	
	//品牌分页查询
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int size) {
		return brandService.findPage(page, size);
	}
	
	//品牌增加
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		try {
			brandService.add(brand);
			return new Result(true, "新增品牌成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "新增品牌失败！");
		}
	}
	
	//加载品牌
	@RequestMapping("findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	
	//修改品牌
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);;
			return new Result(true, "修改品牌成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改品牌失败！");
		}
	}
	
	//批量删除品牌
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除品牌成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除品牌失败！");
		}
		
	}
	
	//品牌组合条件分页查询
	@RequestMapping("search")
	public PageResult search(@RequestBody TbBrand brand,int page, int size) {
		return brandService.findPage(brand, page, size);
	}
	
	//返回品牌下拉列表数据
	@RequestMapping("selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
}
