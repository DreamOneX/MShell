package com.github.asforest.mshell.exception.external

import com.github.asforest.mshell.model.EnvironmentalPreset

class PresetIsIncompeleteException(preset: EnvironmentalPreset)
    : BaseExternalException("环境预设还未配置完毕'${preset.name}'，请检查并完善以下选项: shell, charset")