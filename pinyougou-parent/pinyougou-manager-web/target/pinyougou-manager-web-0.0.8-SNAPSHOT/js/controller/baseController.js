app.controller('baseController' ,function($scope){
	//分页控件配置 
	$scope.paginationConf = {
			currentPage: 1,
			totalItems: 10,
			itemsPerPage: 10,
			perPageOptions: [10, 20, 30, 40, 50],
			onChange: function(){
				$scope.reloadList();
			}
		};
		
		//刷新列表
		$scope.reloadList=function(){
			$scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage );
		}
		
		
		$scope.selectIds=[];//用户勾选的id集合
		//用户勾选复选框
		$scope.updateSelection=function($event,id){
			if($event.target.checked){
				$scope.selectIds.push(id);
			}else{
				var idx = $scope.selectIds.indexOf(id);
		       		$scope.selectIds.splice(idx, 1);//删除 
			}
			
		}
		//
		$scope.jsonToString=function(jsonString,key){
			var json = JSON.parse(jsonString);
			var value="";
			for(var i=0;i<json.length;i++){		
				if(i>0){
					value+=","
				}
				value+=json[i][key];			
			}
			return value;
		}
		//在list集合中查询某key的值的对象
		$scope.searchObjectByKey=function(list,key,keyValue){
			for(var i =0 ; i< list.length; i++){
				if(list[i][key]==keyValue){
					return list[i];
				}
			}
			return null;
		}
});