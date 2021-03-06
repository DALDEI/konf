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

import com.typesafe.config.ConfigFactory
import com.uchuhimo.konf.source.Provider
import com.uchuhimo.konf.source.Source
import java.io.InputStream
import java.io.Reader

/**
 * Provider for HOCON source.
 */
object HoconProvider : Provider {
    override fun fromReader(reader: Reader): Source =
        HoconSource(ConfigFactory.parseReader(reader))

    override fun fromInputStream(inputStream: InputStream): Source =
        fromReader(inputStream.reader())
}
