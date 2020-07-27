package com.flong.kotlin.modules.controller

import com.alibaba.fastjson.JSON
import com.flong.kotlin.core.PageVO
import com.flong.kotlin.core.exception.BaseException
import com.flong.kotlin.core.web.BaseController
import com.flong.kotlin.modules.entity.User
import com.flong.kotlin.modules.enums.UserMsgCode
import com.flong.kotlin.modules.query.UserQuery
import com.flong.kotlin.modules.service.UserService
import com.flong.kotlin.modules.vo.resp.UserRespVo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.redis.core.RedisTemplate


@RestController
@RequestMapping("/rest")
open class UserController : BaseController() {

	@Autowired lateinit var userService: UserService
	@Autowired lateinit var stringRedisTemplate: StringRedisTemplate
	
	//RedisTemplateK, V>这个类由于有K与V，下面的做法是必须要指定Key-Value
	//2 type arguments expected for class RedisTemplate
	@Autowired lateinit var redisTemplate : RedisTemplate<String,Any>
	
	companion object {
		private val log: Logger = LoggerFactory.getLogger(UserController::class.java)
		private final var USER_REDIS_KEY 		= "QUERY_USER_REDIS_KEY"
		private final var USER_REDIS_BACKUP_KEY = "USER_REDIS_BACKUP_KEY"
		private final var CURRENT_USER 			= "CURRENT_USER"
	}

	@RequestMapping("/list1")
	fun list1(): String {
		return "NewFile" //跳转页面
	}

	//添加
	@RequestMapping("/add")
	fun add(): Unit {
		userService.addUser()
	}

	//删除
	@RequestMapping("/deletedById")
	fun deletedById(userId: Long): Unit {
		userService.deleteById(userId)
	}

	//更新
	@RequestMapping("/update")
	fun update(user: User): Unit {
		userService.updateById(user)
	}


	//根据Id查询用户
	@RequestMapping("/getUserId")
	fun getUserId(userId: Long): Any? {
		var user = userService.getUserId(userId)
		if (user == null) {
			var msgCode = UserMsgCode.FIND_NOT_USER
			throw BaseException(msgCode.code!!, msgCode.message!!)
		}
		return userService.getUserId(userId)
	}

	//查询所有
	@RequestMapping("/queryAllUser")
	fun queryAllUser(): List<User>? {
		return userService.queryAllUser()
	}

	//分页查询
	@RequestMapping("listPage")
	fun listPage(query: UserQuery): PageVO<UserRespVo>? {
		var listPage = userService.listPage(query)
		return listPage
	}


	@RequestMapping("/getBody")
	fun getBody(@RequestBody jsonText: UserRespVo) {
		println(jsonText.component2())
		println(jsonText.userName)
	}

	//简单的缓存测试
	@RequestMapping("/getUserByRedis/{userId}")
	fun getUserByRedis(@PathVariable("userId") userId: Long) {
		var redis_key 	= USER_REDIS_KEY + "_" + userId
		var user		= stringRedisTemplate.opsForValue().get(redis_key)
		if (user == null) {
			var userObj  = userService.getUserId(userId)
			stringRedisTemplate.opsForValue().set(redis_key, userObj.toString())
			print("从DB获取----" + JSON.toJSONString(userObj))

		} else {
			print("从缓存获取----" + JSON.toJSONString(user))
		}

	}
	
	//简单的缓存测试
	@RequestMapping("/getUserByRedis1/{userId}")
	fun getUserByRedis1(@PathVariable("userId") userId: Long) {
		var redis_key 	= USER_REDIS_KEY + "_" + userId
		var user		= redisTemplate.opsForValue().get(redis_key)
		if (user == null) {
			var userObj  = userService.getUserId(userId)
			redisTemplate.opsForValue().set(redis_key, userObj.toString())
			print("从DB获取----" + JSON.toJSONString(userObj))

		} else {
			print("从缓存获取----" + JSON.toJSONString(user))
		}

	}

}