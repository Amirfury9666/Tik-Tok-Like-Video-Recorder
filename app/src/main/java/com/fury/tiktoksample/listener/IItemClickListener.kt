package com.fury.tiktoksample.listener

interface IItemClickListener<T> {
    fun onItemClick(item: T, position: Int)
}