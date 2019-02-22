 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$scope.reg=function(){
		//匹配朗次输入密码是否一致
		if($scope.entity.password!=$scope.password)  {
		      	alert("两次输入的密码不一致，请重新输入");	
		      	$scope.entity.password="";
		      	$scope.password="";
		      	return ;
		    } 		
		userService.add($scope.entity).success(
				function(response){
					alert(response.message);
				}
				
		);
	}
    
});	
