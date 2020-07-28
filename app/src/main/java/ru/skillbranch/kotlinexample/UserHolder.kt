package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder  {
    val map = mutableMapOf<String, User>()

    fun registerUser(fullName:String, email : String, password : String) : User {
        return User.makeUser(fullName, email, password).also {
            require(!map.contains(it.login)) { println("A user with this email already exists")}
            map[it.login] = it
        }
    }



    fun registerUserByPhone(fullName: String, rawPhone: String) : User {
        if (rawPhone.matches("\\+\\d \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}".toRegex()))
            return User.makeUser(fullName, null, null, rawPhone.trimPhone()).also {
                require(!map.contains(it.login)) { println("A user with this phone already exists")}
                map[it.login] = it
            }
        else throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
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