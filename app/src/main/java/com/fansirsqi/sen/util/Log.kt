package com.fansirsqi.sen.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Log {
    private val logger: Logger = LoggerFactory.getLogger("sen")

    fun v(tag: String, msg: String) {
        logger.trace("[$tag] $msg")
    }

    fun d(tag: String, msg: String) {
        logger.debug("[$tag] $msg")
    }

    fun i(tag: String, msg: String) {
        logger.info("[$tag] $msg")
    }

    fun w(tag: String, msg: String) {
        logger.warn("[$tag] $msg")
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) logger.error("[$tag] $msg", throwable)
        else logger.error("[$tag] $msg")
    }
}
