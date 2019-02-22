package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService{
	
	@Autowired
	private TbItemMapper itemMapper;

	//添加商品到购物车
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1..根据skuID查询sku对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态不合法");
		}
		//2..根据sku对象得到商家ID
		String sellerId = item.getSellerId();
		//3..根据商家id在购物车列表中查找购物车对象
		Cart cart = searchCartBySellerId(cartList,sellerId);
		if(cart==null) {//4..如果购物车列表中不存在该商家的购物车
			
			//4.1..创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			List<TbOrderItem> orderItemList=new ArrayList<TbOrderItem>();
			TbOrderItem orderItem = createOrderItem(item,num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//4.2..将新的购物车添加到购物车列表中
			cartList.add(cart);
			
		}else {
			//5..如果购物车列表中存在该商家的购物车
			
			//5.1..判断该商品是否在该购物车的明细列表中存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem==null) {
				//5.11...如果不存在创建新的购物车明细对象，并添加到明细列表中
				orderItem=createOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
			}else {
				
				//5.12...如果存在，在原有的数量上添加数量，并且更新金额
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()) );
				if(orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				if(cart.getOrderItemList().size()==0) {
					cartList.remove(cart);
				}
			}
			
		
		}
		
		
		
		
		return cartList;
	}
	
	//根据商家id在购物车列表中查找购物车对象
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}

	//根据skuid在购物车明细列表中查询购物车明细对象
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId ) {
		for (TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}

	//创建购物车明细对象
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		TbOrderItem orderItem= new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	//从redis里去购物车列表
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车"+username);
		List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null) {
			cartList=new ArrayList<>();
		}
		return cartList;
	}

	//往redis里存购物车列表
	public void saveCartListToRedis(String username, List<Cart> cartlist) {
		System.out.println("往redis中存入购物车"+username);
		redisTemplate.boundHashOps("cartList").put(username,cartlist);
	}

	//合并购物车
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		for (Cart cart : cartList2) {
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());		
			}			
		}
		return cartList1;
	}
}
