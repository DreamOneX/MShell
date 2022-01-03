package com.github.asforest.mshell.session.user

import com.github.asforest.mshell.session.SessionUser
import net.mamoe.mirai.contact.User

class FriendUser(val user: User) : SessionUser()
{
    override suspend fun onSendMessage(message: String)
    {
        user.sendMessage(message)
    }

    override fun toString(): String
    {
        return "${user.nick}(${user.id})"
    }

    override fun equals(other: Any?): Boolean
    {
        if (other == null || other !is SessionUser)
            return false

        return toString() == other.toString()
    }

    override fun hashCode(): Int
    {
        val i = toString().hashCode()
        return (i shl 5) - i
    }
}