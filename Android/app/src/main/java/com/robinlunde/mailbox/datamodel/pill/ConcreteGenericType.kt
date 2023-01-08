package com.robinlunde.mailbox.datamodel.pill

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class ConcreteGenericType(
    override val id: Int? = null,
    override val msg: String? = null
) : GenericType<ConcreteGenericType>() {
    override fun get(): ConcreteGenericType {
        return this
    }
}