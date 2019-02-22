package com.pinyougou.search.service;


import java.util.List;
import java.util.Map;

/**
 * 搜索方法
 * */
public interface ItemSearchService {
	
	//搜索
	public Map search(Map searchMap);
	
	//数据导入
	public void importList(List list);
	
	//删除商品列表
	public void deleteByGoodsIds(List goodsIds);
}
