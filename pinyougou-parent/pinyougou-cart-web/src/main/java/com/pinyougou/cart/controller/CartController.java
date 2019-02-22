package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.CachingResourceTransformer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference
	private CartService cartService;
	
	@Autowired
	private HttpServletResponse response;
	
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(name);
		
		try {
			//1..从提取购物车
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if(name.equals("anonymousUser")) {
				//未登录
				//2..调用服务方法操作购物车
			
				String cartListString = JSON.toJSONString(cartList);
				//3..将新的购物车存入cookie
				util.CookieUtil.setCookie(request, response, "cartList", cartListString, 3600*24, "utf-8");
				
			}else {
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true,"存入购物车成功");
		} catch (Exception e) {
			return new Result(false,"存入购物车失败");
		}
	
		
	}
	
	@Autowired
	private HttpServletRequest request;
	
	
	//从cookie中提取购物车
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(name);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "utf-8");
		if(cartListString==null || cartListString.equals("")){
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		if(name.equals("anonymousUser")) {
			//未登录
			
			return cartList_cookie;
		}else {
			List<Cart> list = cartService.findCartListFromRedis(name);
			if(cartList_cookie.size()>0) {
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, list);
				cartService.saveCartListToRedis(name, cartList);
				util.CookieUtil.deleteCookie(request, response, "cartList");
				return cartList;
			}
			return list;
		
		}
		
	}
}
