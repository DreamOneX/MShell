package com.github.asforest.mshell.command.parser

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import kotlin.reflect.*
import kotlin.reflect.full.*

@OptIn(ExperimentalCommandDescriptors::class)
@ConsoleExperimentalApi
abstract class MultiLayerCommand(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    override val owner: CommandOwner,

    /** 主指令名. */
    override val primaryName: String,

    /** 次要指令名. */
    override vararg val secondaryNames: String,

    /** 用法说明, 用于发送给用户 */
    override val usage: String = "<no usages given>",

    /** 指令描述, 用于显示在 [BuiltInCommands.HelpCommand] */
    override val description: String = "<no descriptions given>",

    /** 指令父权限 */
    parentPermission: Permission = owner.parentPermission,

    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    override val prefixOptional: Boolean = false,
) : RawCommand(
    owner = owner,
    primaryName = primaryName,
    secondaryNames = secondaryNames,
    usage = usage,
    description = description,
    parentPermission = parentPermission,
    prefixOptional = prefixOptional
) {
    private val reflector by lazy { CommandReflector(this, AnnotationResolver()) }

    override suspend fun CommandSender.onCommand(args: MessageChain)
    {

    }

    override val overloads: List<CommandSignature> by lazy {
        reflector.findSubCommands()
    }

    /**
     * 子指令声明
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    protected annotation class SubCommand

    /**
     * 子指令描述
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    protected annotation class Description(val desc: String)

    /** 参数名, 将参与构成 [usage] */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    protected annotation class Name(val name: String)

    class AnnotationResolver : SubCommandAnnotationResolver
    {
        override fun hasAnnotation(ownerCommand: Command, function: KFunction<*>): Boolean
        {
            return function.hasAnnotation<SubCommand>()
        }

        override fun getSubCommandNames(ownerCommand: Command, function: KFunction<*>): Array<out String>
        {
            return arrayOf(function.name)
        }

        override fun getAnnotatedName(ownerCommand: Command, parameter: KParameter): String?
        {
            return parameter.findAnnotation<Name>()?.name
        }

        override fun getDescription(ownerCommand: Command, function: KFunction<*>): String?
        {
            return function.findAnnotation<Description>()?.desc
        }
    }

}


