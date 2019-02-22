package com.pinyougou.page.service;

public interface ItemPageService {

	//根据商品编号生产商品详细页
	public boolean genItemHtml(Long goodsId);
	//根据商品编号删除商品详细页
	public boolean deleteItemHtml(Long[] goodsIds);
}
