package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{

	@Autowired
	private SolrTemplate solrTemplate;
	/**
	 * 搜索功能
	 * */
	public Map search(Map searchMap) {
		
		//空格处理
		String keywords =(String) searchMap.get("keywords");
		searchMap.put("keywords",keywords.replace(" ", ""));
		Map map = new HashMap();
		
		//查询列表
		map.putAll(searchList(searchMap));
		//分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//查询品牌和规格列表
		String  category = (String) searchMap.get("category");
		if(!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		}else {
			if(categoryList.size()>0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		return map;
	}

	@Autowired
	private RedisTemplate  redisTemplate;
	//根据商品分类名称查询品牌和规格列表
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		//根据商品名称获取模板ID
		Long templateId =(Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId!=null) {
			//根据模板ID获取品牌列表
			List brandList =(List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			//根据模板ID获取规格列表
			List specList =(List)redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
	}
	
	//分组查询商品分类列表（查询分类列表）
	private List searchCategoryList(Map searchMap) {
		List list = new ArrayList();
		Query query = new SimpleQuery("*:*");
		//关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions= new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query ,TbItem.class);
		//获得分组结果
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> groupEntry : content) {
			//将分组的结果添加到List集合中
			list.add(groupEntry.getGroupValue());
		}
		return list;
	}
	//查询列表
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		//高亮显示初始化
		HighlightQuery query = new SimpleHighlightQuery();
		//高亮列
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//后缀
		highlightOptions.setSimplePostfix("</em>");
		//为查询对象设置高亮选项
		query.setHighlightOptions(highlightOptions);
		//关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		/********************************************************/
		//按照商品分类过滤
		
		if(!"".equals(searchMap.get("category"))){			
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//按照品牌过滤
		
		if(!"".equals(searchMap.get("brand"))){			
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		
		//按规格过滤
		if(searchMap.get("spec") !=null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key : specMap.keySet()) {
				Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//按照价格过滤
		if(!"".equals(searchMap.get("price"))){			
			String  priceStr = (String) searchMap.get("price");
			String[] price = priceStr.split("-");
			if(!price[0].equals("0")) {
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if(!price[1].equals("*")) {
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		
		
		//分页
		//获取页码
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if(pageNo==null) {
			pageNo = 1 ;
		}
		//获取页大小
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if(pageSize==null) {
			pageSize = 10 ;
		}
		//起始索引
		query.setOffset( (pageNo-1)*pageSize );
		//每页记录数
		query.setRows(pageSize);
		
		
		
		//排序
		String sortValue = (String) searchMap.get("sort");//升序ASC  降序DESC
		String sortFiled = (String) searchMap.get("sortFiled");//排序字段
		if(sortValue!=null && !sortValue.equals("")) {
			if(sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortFiled);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortFiled);
				query.addSort(sort);
			}
		}
		
		/***********************获取高亮结果集*********************************/
		//返回高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表
			List<Highlight> highlightList = entry.getHighlights();
					
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0) {

				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}	
					
		}
		map.put("rows", page.getContent());
		//总页数
		map.put("totalPages",page.getTotalPages());
		//总条数
		map.put("total", page.getTotalElements());
		return map;
	}

	//数据导入
	public void importList(List list) {
	
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	//删除商品列表
	public void deleteByGoodsIds(List goodsIds) {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
