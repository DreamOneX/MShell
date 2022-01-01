package com.github.asforest.mshell.command

import com.github.asforest.mshell.MShellPlugin
import com.github.asforest.mshell.command.parser.MultiLayerCommand
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@ConsoleExperimentalApi
object UserCommand : MultiLayerCommand(
    MShellPlugin,
    primaryName = "w",
    description = "MShell插件授权用户常规指令",
    secondaryNames = arrayOf("msu", "mu"),
) {
//    suspend fun CommandSender.
}