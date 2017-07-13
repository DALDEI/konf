/*
 * Copyright 2017 the original author or authors.
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

package com.uchuhimo.konf

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

sealed class Item<T : Any>(
        val spec: ConfigSpec,
        name: String,
        val description: String = "") {
    init {
        spec.addItem(this)
    }

    val name: String = spec.qualify(name)

    val path: Path = run {
        val path = this.name.split('.')
        check("" !in path) { "${this.name} is invalid name for item" }
        path
    }

    val type: JavaType = TypeFactory.defaultInstance().constructType(this::class.java)
            .findSuperType(Item::class.java).bindings.typeParameters[0]

    open val isRequired: Boolean get() = false

    open val isOptional: Boolean get() = false

    open val isLazy: Boolean get() = false

    val asRequiredItem: RequiredItem<T> get() = this as RequiredItem<T>

    val asOptionalItem: OptionalItem<T> get() = this as OptionalItem<T>

    val asLazyItem: LazyItem<T> get() = this as LazyItem<T>
}

typealias Path = List<String>

val Path.name: String get() = joinToString(".")

fun String.toPath(): Path = listOf(this)

open class RequiredItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        description: String = ""
) : Item<T>(spec, name, description) {
    override val isRequired: Boolean = true
}

open class OptionalItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        val default: T,
        description: String = ""
) : Item<T>(spec, name, description) {
    override val isOptional: Boolean = true
}

open class LazyItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        val thunk: (ConfigGetter) -> T,
        val placeholder: String = "",
        description: String = ""
) : Item<T>(spec, name, description) {
    override val isLazy: Boolean = true
}
