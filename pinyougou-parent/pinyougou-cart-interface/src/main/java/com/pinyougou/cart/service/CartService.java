package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

/**
 * 购物车服务接口
 * */
public interface CartService {
	//添加商品到购物车列表
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

	//从redis取购物车列表
	public List<Cart> findCartListFromRedis(String username);
	
	//往redis里存购物车l列表
	public void saveCartListToRedis(String username,List<Cart> cartlist);

	//合并购物车
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
