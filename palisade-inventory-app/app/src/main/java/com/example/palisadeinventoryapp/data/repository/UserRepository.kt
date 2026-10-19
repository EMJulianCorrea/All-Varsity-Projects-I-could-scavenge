package com.example.palisadeinventoryapp.data.repository

import com.example.palisadeinventoryapp.data.dao.UserDao
import com.example.palisadeinventoryapp.data.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun insert(user: User): Long = userDao.insert(user)

    suspend fun update(user: User) = userDao.update(user)

    suspend fun delete(user: User) = userDao.delete(user)

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    suspend fun login(username: String, hashedPassword: String): User? =
        userDao.login(username, hashedPassword)

    suspend fun getUserByUsername(username: String): User? =
        userDao.getUserByUsername(username)
}
