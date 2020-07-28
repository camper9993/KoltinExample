package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder  {
    val map = mutableMapOf<String, User>()

    fun registerUser(fullName:String, email : String, password : String) : User {
        return User.makeUser(fullName, email, password).also {
            if (map.contains(it.login)) {
                throw IllegalArgumentException("A user with this email already exists")
            }
            else
                map[it.login] = it
        }
    }



    fun registerUserByPhone(fullName: String, rawPhone: String) : User {
        if (rawPhone.trimPhone().matches("\\+\\d{11}".toRegex()))
            return User.makeUser(fullName, null, null, rawPhone.trimPhone()).also {
                require(!map.contains(it.login)) { println("A user with this phone already exists")}
                map[it.login] = it
            }
        else
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
    }

    fun loginUser (login : String, password: String) : String? {
        return map[login.trimPhone()]?.let {
            if (it.checkPassword(password)) it.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String) : Unit {
        map[login.trimPhone()]?.also {
            val oldPass = it.accessCode
            it.accessCode = it.generateAccessCode()
            it.changePassword(oldPass!!, it.accessCode!!)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

    private fun String.trimPhone () : String {
        return this.replace("[ ()-]+".toRegex(), replacement = "")
    }
}