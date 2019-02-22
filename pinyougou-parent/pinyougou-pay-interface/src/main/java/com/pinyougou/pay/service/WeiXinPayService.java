package com.pinyougou.pay.service;

import java.util.Map;

public interface WeiXinPayService {

	//生成二维码
	public Map createNative(String out_trade_no,String total_fee);
	
}
