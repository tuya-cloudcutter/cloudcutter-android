/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-19.
 */

package io.github.cloudcutter.work.common

abstract class ByteStruct {

	abstract fun getValues(): List<Any>

	fun getFormat(): String =
		this::class.java.getAnnotation(ByteStructFormat::class.java)!!.format

	companion object {
		inline fun <reified T : ByteStruct> unpack(data: ByteArray, mode: Int = 0) =
			unpackImpl(data, T::class.java, mode)

		fun pack(data: ByteStruct, mode: Int = 0): ByteArray {
			val cls = data::class.java
			// retrieve child type annotations
			val childTypeMap = cls.getAnnotation(ByteStructChildMap::class.java)?.map?.filter {
				it.mode == mode
			}

			// retrieve format string and get struct values
			val formatString = cls.getAnnotation(ByteStructFormat::class.java)!!.format
			val values = data.getValues().toMutableList()

			// update child type fields
			for ((index, value) in values.withIndex()) {
				if (value is String) {
					values[index] = value.toByteArray()
					continue
				}
				if (value !is ByteStruct)
					continue
				values[index] = pack(value)
				val annotation = childTypeMap?.firstOrNull {
					index == it.dataFieldIndex && value::class == it.dataFieldClass
				} ?: continue
				values[annotation.typeFieldIndex] = annotation.typeFieldValue
			}

			return Struct.pack(formatString, values)
		}

		fun <T : ByteStruct> unpackImpl(data: ByteArray, cls: Class<T>, mode: Int = 0): T {
			// retrieve child type annotations
			val childTypeMap = cls.getAnnotation(ByteStructChildMap::class.java)?.map?.filter {
				it.mode == mode
			}

			// retrieve format string and unpack the struct
			val formatString = cls.getAnnotation(ByteStructFormat::class.java)!!.format
			val values = Struct.unpack(formatString, data).toMutableList()

			// deserialize all child ByteStruct fields
			val constructor = cls.constructors.first()
			for ((index, type) in constructor.parameterTypes.withIndex()) {
				if (String::class.java.isAssignableFrom(type)) {
					values[index] = (values[index] as ByteArray).decodeToString()
					continue
				}
				if (!ByteStruct::class.java.isAssignableFrom(type))
					continue
				val annotation = childTypeMap?.firstOrNull {
					index == it.dataFieldIndex && values[it.typeFieldIndex] == it.typeFieldValue
				}
				val childType = annotation?.dataFieldClass?.java ?: type
				values[index] = unpackImpl(values[index] as ByteArray, childType as Class<T>)
			}

			return constructor.newInstance(*values.toTypedArray()) as T
		}
	}
}
