package com.todo.admin.repository

import com.todo.admin.domain.entity.UserEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface UserRepository : CrudRepository<UserEntity, String>
