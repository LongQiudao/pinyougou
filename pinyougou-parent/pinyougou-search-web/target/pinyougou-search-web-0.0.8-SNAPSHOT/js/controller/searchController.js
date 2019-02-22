app.controller('searchController',function($location,$scope,searchService){	
	
	//定义搜索对象的结构
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortFiled':'' };//搜索对象
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
		   function(response){
			   $scope.resultMap=response;//搜索返回的结果 
			   
			   buildPageLabel();//构建分页栏
			
			   
		   });
	}
	
	
	buildPageLabel=function(){
		//构建分页栏
		$scope.pageLabel=[];
		//开始页码
		var firstPage=1;
		//结束页码
		var lastPage=$scope.resultMap.totalPages;
		//前面有点
		$scope.firstDot=true;
		//后面有点
		$scope.lastDot=true;
		if($scope.resultMap.totalPages>5){//如果页码数量大于5
			if($scope.searchMap.pageNo<=3){//如果当前页码小鱼三显示前5项
				$scope.firstDot=false;//前面没点后面有点
				lastPage=5;
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){////如果当前页码大于最大页码-2，显示后五项
				firstPage=$scope.resultMap.totalPages-4;
				$scope.lastDot=false;//后面有点前面没点
			}else{
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;
			$scope.lastDot=false;//前后都有点
		}
		//构建页码
		for(var i=firstPage;i<= lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	//添加搜索项,改变searchMap的值
	$scope.addSearchItem=function(key,value){
		//如果用户点击的是分类或品牌
		if(key=='category'|| key == 'brand' || key =='price'){
			$scope.searchMap[key] = value;
		}else{//用户点击的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category'|| key == 'brand' || key =='price'){
			$scope.searchMap[key] = "";
		}else{//用户点击的是规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
	
	//分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return ;
		}
		$scope.searchMap.pageNo= pageNo;
		$scope.search();//查询
	}
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	
	//排序排序
	$scope.sortSearch=function(sortFiled,sort){
		$scope.searchMap.sortFiled=sortFiled;
		$scope.searchMap.sort=sort;
		$scope.search();//查询
	}
	
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf( $scope.resultMap.brandList[i].text )>=0){
				return true;
			}
		}
		return false;
	}
	
	
	//加载关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=  $location.search()['keywords'];
		$scope.search();
	}
	
});