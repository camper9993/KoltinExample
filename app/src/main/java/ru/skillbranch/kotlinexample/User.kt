package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName : String,
    private val lastName : String?,
    email : String? = null,
    rawPhone : String? = null,
    meta : Map<String, Any>? = null
) {
    val userInfo : String

    private val fullName : String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").
                capitalize()
    private val initials : String
        get() = listOfNotNull(firstName, lastName)
            .map {it.first().toUpperCase()}.joinToString(" ")

    private var phone : String? = null
        set(value) {
            field = value?.replace("[ ()-]+".toRegex(), replacement = "")
        }

    private var _login : String? = null
    var login : String
        set(value) {
            _login = value.toLowerCase()
        }
    get() = _login!!

    private val salt : String by lazy {
        ByteArray(size = 16).also {
            SecureRandom().nextBytes(it)
        }.toString()
    }
    private lateinit var passwordHash : String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null


    constructor(
        firstName : String,
        lastName : String?,
        email : String,
        password : String
    ) : this(firstName, lastName, email, meta = mapOf("auth" to "password")) {
        println("Secondary mail constructor")
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        val code = generateAccessCode()
        accessCode = code
        passwordHash = encrypt(accessCode!!)
        sendAccessCodeToUser(rawPhone, accessCode!!)
    }



    init {
        println("First init block, primary constructor was called")

        require(!firstName.isBlank()) {"First name can't be empty"}
        require(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {"Email or phone can't be empty"}

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """ 
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass : String) = encrypt(pass) == passwordHash

    fun changePassword(oldPass : String, newPass : String) {
        if (checkPassword(oldPass))
            passwordHash = encrypt(newPass)
        else throw IllegalArgumentException ("The entered password does not match current password")
    }

    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println(". . . . sending access code: $code on $phone")
    }

    private fun encrypt(password: String): String = password.md5()

    public fun generateAccessCode(): String {
        val possible = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also {
                    append(possible[it])
                }
            }
        }.toString()
    }

    private fun String.md5() : String{
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }
    companion object Factory {
        fun makeUser (
            fullName : String,
            email : String? = null,
            password : String? = null,
            phone : String? = null
        ):User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone )
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }
    }
}

private fun String.fullNameToPair(): Pair<String, String?> {
    return this.split(" ")
        .filter { it.isNotBlank() }
        .run {
            when(size) {
                1 -> first() to null
                2 -> first() to last()
                else -> throw IllegalArgumentException("Full name must contain only first name and last name, current split result ${this@fullNameToPair}")
            }
        }
}
