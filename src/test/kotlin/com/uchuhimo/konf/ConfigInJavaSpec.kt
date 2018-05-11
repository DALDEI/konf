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

package com.uchuhimo.konf

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object ConfigInJavaSpec : SubjectSpek<Config>({

    val spec = NetworkBufferInJava.spec
    val size = NetworkBufferInJava.size
    val maxSize = NetworkBufferInJava.maxSize
    val name = NetworkBufferInJava.name
    val type = NetworkBufferInJava.type

    subject { Config { addSpec(spec) } }

    given("a config") {
        val invalidItem by ConfigSpec("invalid").run { required<Int>() }
        group("addSpec operation") {
            on("add orthogonal spec") {
                val newSpec = object : ConfigSpec(spec.prefix) {
                    val minSize by optional(1)
                }
                subject.addSpec(newSpec)
                it("should contain items in new spec") {
                    assertThat(newSpec.minSize in subject, equalTo(true))
                    assertThat(subject.nameOf(newSpec.minSize) in subject, equalTo(true))
                }
                it("should contain new spec") {
                    assertThat(newSpec in subject.specs, equalTo(true))
                    assertThat(spec in subject.specs, equalTo(true))
                }
            }
            on("add repeated item") {
                it("should throw RepeatedItemException") {
                    assertThat({ subject.addSpec(spec) }, throws(has(
                        RepeatedItemException::name,
                        equalTo(subject.nameOf(size)))))
                }
            }
            on("add repeated name") {
                val newSpec = ConfigSpec(spec.prefix).apply {
                    @Suppress("UNUSED_VARIABLE", "NAME_SHADOWING")
                    val size by required<Int>()
                }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(newSpec) }, throws<NameConflictException>())
                }
            }
            on("add conflict name, which is prefix of existed name") {
                val newSpec = ConfigSpec("network").apply {
                    @Suppress("UNUSED_VARIABLE")
                    val buffer by required<Int>()
                }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(newSpec) }, throws<NameConflictException>())
                }
            }
            on("add conflict name, and an existed name is prefix of it") {
                val newSpec = ConfigSpec(subject.nameOf(type)).apply {
                    @Suppress("UNUSED_VARIABLE")
                    val subType by required<Int>()
                }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(newSpec) }, throws<NameConflictException>())
                }
            }
        }
        on("iterate items in config") {
            it("should cover all items in config") {
                assertThat(subject.items.toSet(), equalTo(spec.items.toSet()))
            }
        }
        group("get operation") {
            on("get with valid item") {
                it("should return corresponding value") {
                    assertThat(subject[name], equalTo("buffer"))
                }
            }
            on("get with invalid item") {
                it("should throw NoSuchItemException when using `get`") {
                    assertThat({ subject[invalidItem] },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName))))
                }
                it("should return null when using `getOrNull`") {
                    assertThat(subject.getOrNull(invalidItem), absent())
                }
            }
            on("get with valid name") {
                it("should return corresponding value") {
                    assertThat(subject(spec.qualify("name")), equalTo("buffer"))
                }
            }
            on("get with invalid name") {
                it("should throw NoSuchItemException when using `get`") {
                    assertThat({ subject<String>(spec.qualify("invalid")) }, throws(has(
                        NoSuchItemException::name, equalTo(spec.qualify("invalid")))))
                }
                it("should return null when using `getOrNull`") {
                    assertThat(subject.getOrNull<String>(spec.qualify("invalid")), absent())
                }
            }
            on("get unset item") {
                it("should throw UnsetValueException") {
                    assertThat({ subject[size] }, throws(has(
                        UnsetValueException::name,
                        equalTo(size.asName))))
                    assertThat({ subject[maxSize] }, throws(has(
                        UnsetValueException::name,
                        equalTo(size.asName))))
                }
            }
        }
        group("set operation") {
            on("set with valid item when corresponding value is unset") {
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[size], equalTo(1024))
                }
            }
            on("set with valid item when corresponding value exists") {
                subject[name] = "newName"
                it("should contain the specified value") {
                    assertThat(subject[name], equalTo("newName"))
                }
            }
            on("set with valid item when corresponding value is lazy") {
                test("before set, the item should be lazy; after set," +
                    " the item should be no longer lazy, and it contains the specified value") {
                    subject[size] = 1024
                    assertThat(subject[maxSize], equalTo(subject[size] * 2))
                    subject[maxSize] = 0
                    assertThat(subject[maxSize], equalTo(0))
                    subject[size] = 2048
                    assertThat(subject[maxSize], !equalTo(subject[size] * 2))
                    assertThat(subject[maxSize], equalTo(0))
                }
            }
            on("set with invalid item") {
                it("should throw NoSuchItemException") {
                    assertThat({ subject[invalidItem] = 1024 },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName))))
                }
            }
            on("set with valid name") {
                subject[spec.qualify("size")] = 1024
                it("should contain the specified value") {
                    assertThat(subject[size], equalTo(1024))
                }
            }
            on("set with invalid name") {
                it("should throw NoSuchItemException") {
                    assertThat({ subject[invalidItem] = 1024 },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName))))
                }
            }
            on("set with incorrect type of value") {
                it("should throw ClassCastException") {
                    assertThat({ subject[subject.nameOf(size)] = "1024" }, throws<ClassCastException>())
                }
            }
            on("lazy set with valid item") {
                subject.lazySet(maxSize) { it[size] * 4 }
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[maxSize], equalTo(subject[size] * 4))
                }
            }
            on("lazy set with valid name") {
                subject.lazySet(subject.nameOf(maxSize)) { it[size] * 4 }
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[maxSize], equalTo(subject[size] * 4))
                }
            }
            on("lazy set with valid name and invalid value with incompatible type") {
                subject.lazySet(subject.nameOf(maxSize)) { "string" }
                it("should throw InvalidLazySetException when getting") {
                    assertThat({ subject[subject.nameOf(maxSize)] }, throws<InvalidLazySetException>())
                }
            }
            on("unset with valid item") {
                subject.unset(type)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(type), absent())
                }
            }
            on("unset with valid name") {
                subject.unset(subject.nameOf(type))
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(type), absent())
                }
            }
        }
        group("item property") {
            on("declare a property by item") {
                var nameProperty by subject.property(name)
                it("should behave same as `get`") {
                    assertThat(nameProperty, equalTo(subject[name]))
                }
                it("should support set operation as `set`") {
                    nameProperty = "newName"
                    assertThat(nameProperty, equalTo("newName"))
                }
            }
            on("declare a property by name") {
                var nameProperty by subject.property<String>(subject.nameOf(name))
                it("should behave same as `get`") {
                    assertThat(nameProperty, equalTo(subject[name]))
                }
                it("should support set operation as `set`") {
                    nameProperty = "newName"
                    assertThat(nameProperty, equalTo("newName"))
                }
            }
        }
    }
})
