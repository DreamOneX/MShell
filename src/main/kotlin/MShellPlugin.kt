package com.github.asforest.mshell

import com.github.asforest.mshell.command.AdminsCommand
import com.github.asforest.mshell.command.EnvCommand
import com.github.asforest.mshell.command.MainCommand
import com.github.asforest.mshell.configuration.ConfigProxy
import com.github.asforest.mshell.configuration.EnvironmentPresets
import com.github.asforest.mshell.configuration.MainConfig
import com.github.asforest.mshell.exception.BaseException
import com.github.asforest.mshell.exception.external.BaseExternalException
import com.github.asforest.mshell.permission.MShellPermissions
import com.github.asforest.mshell.session.SessionManager
import com.github.asforest.mshell.session.SessionUser
import com.github.asforest.mshell.util.MiraiUtil
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.PokeMessage
import net.mamoe.mirai.message.data.content


object MShellPlugin : KotlinPlugin(MiraiUtil.pluginDescription)
{
    val ep = ConfigProxy(EnvironmentPresets::class.java, "presets.yml")

    override fun onEnable()
    {
        MShellPermissions.all

        ep.read()
        MainConfig.reload()
        MainCommand.register()
        EnvCommand.register()
        AdminsCommand.register()

        // 订阅好友消息
        GlobalEventChannel.filter { it is BotEvent }.subscribeAlways<FriendMessageEvent> {
            if (!toCommandSender().hasPermission(MShellPermissions.all) )
                return@subscribeAlways

            withCatch {
                val user = SessionUser(user)
                val pokePresent = message.filterIsInstance<PokeMessage>().isNotEmpty()
                val session = SessionManager.getSessionByUserConnected(user)

                if(pokePresent)
                {
                    if(session != null)
                    {
                        session.disconnect(user)
                    } else {
                        SessionManager.reconnectOrCreate(user)
                    }
                } else {
                    val message = message.content

                    if(session != null)
                    {
                        val inputPrefix = MainConfig.explicitInputPrefix

                        if(inputPrefix.isNotEmpty() && // 以inputPrefix开头且有内容
                            message.startsWith(inputPrefix) &&
                            message.length > inputPrefix.length)
                        {
                            val content = message.substring(inputPrefix.length)
                            session.stdin.println(content)
                        } else {
                            session.stdin.println(message)
                        }
                    }
                }
            }
        }
    }

    suspend inline fun FriendMessageEvent.withCatch(block: FriendMessageEvent.() -> Unit)
    {
        try { block() } catch (e: BaseExternalException) { user.sendMessage(e.message ?: e.stackTraceToString()) }
    }

}