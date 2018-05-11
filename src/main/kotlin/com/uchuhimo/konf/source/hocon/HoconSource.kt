/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uchuhimo.konf.source.hocon

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.SourceInfo

/**
 * Source from a HOCON map.
 */
class HoconSource(
    val config: Config,
    context: Map<String, String> = mapOf()
) : Source, SourceInfo by SourceInfo.with(context) {
    init {
        addInfo("type", "HOCON")
    }

    override fun contains(path: Path): Boolean = config.hasPathOrNull(path.name)

    override fun getOrNull(path: Path): Source? {
        val name = path.name
        return if (config.hasPathOrNull(name)) {
            if (config.getIsNull(name)) {
                HoconValueSource(
                    ConfigValueFactory.fromAnyRef(null, config.origin().description()),
                    context)
            } else {
                HoconValueSource(config.getValue(name), context)
            }
        } else {
            null
        }
    }
}
