package com.fury.tiktoksample.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fury.tiktoksample.R
import com.fury.tiktoksample.data.VideoFilter
import com.fury.tiktoksample.databinding.ItemFilterBinding
import com.fury.tiktoksample.extension.filtersAsList
import com.fury.tiktoksample.extension.makeFirstLatterCapital
import com.fury.tiktoksample.extension.setTextOnView
import com.fury.tiktoksample.listener.IItemClickListener
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created By Amir Fury On 24 November
 *
 * Email fury.amir93@gmail.com
 */

class FiltersAdapter constructor(
    context: Context,
    private val listener: IItemClickListener<VideoFilter>?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val filters = arrayListOf<VideoFilter>()
    private val gpuImage = GPUImage(context)


    fun submitFilters(bitmap: Bitmap, list: ArrayList<VideoFilter>) {
        gpuImage.setImage(bitmap)
        list.forEach {
            filters.add(it)
            notifyItemInserted(filters.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FiltersViewHolder(ItemFilterBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val filter = filters[position]
        (holder as FiltersViewHolder).bind(filter)
    }

    override fun getItemCount(): Int = filters.size

    private inner class FiltersViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoFilter) {
            setFilters(item)
            val filterName = item.name.lowercase(Locale.US)
            binding.apply {
                name.setTextOnView(filterName.makeFirstLatterCapital())
                image.setImageBitmap(gpuImage.bitmapWithFilterApplied)
            }
            itemView.setOnClickListener {
                listener?.onItemClick(item, absoluteAdapterPosition)
            }
        }

        private fun setFilters(filter: VideoFilter) {
            when (filter) {
                VideoFilter.BRIGHTNESS -> {
                    val glf = GPUImageBrightnessFilter()
                    glf.setBrightness(2.0f)
                    gpuImage.setFilter(glf)
                }
                VideoFilter.EXPOSURE -> gpuImage.setFilter(GPUImageExposureFilter())
                VideoFilter.GAMMA -> gpuImage.setFilter(GPUImageGammaFilter())
                VideoFilter.GRAYSCALE -> gpuImage.setFilter(GPUImageGrayscaleFilter())
                VideoFilter.HAZE -> {
                    val glf = GPUImageHazeFilter()
                    glf.setSlope(-0.5f)
                    gpuImage.setFilter(glf)
                }
                VideoFilter.INVERT -> gpuImage.setFilter(GPUImageColorInvertFilter())
                VideoFilter.MONOCHROME -> gpuImage.setFilter(GPUImageMonochromeFilter())
                VideoFilter.PIXELATED -> {
                    val glf = GPUImagePixelationFilter()
                    glf.setPixel(5f)
                    gpuImage.setFilter(glf)
                }
                VideoFilter.POSTERIZE -> gpuImage.setFilter(GPUImagePosterizeFilter())
                VideoFilter.SEPIA -> gpuImage.setFilter(GPUImageSepiaToneFilter())
                VideoFilter.SHARP -> {
                    val glf = GPUImageSharpenFilter()
                    glf.setSharpness(1f)
                    gpuImage.setFilter(glf)
                }
                VideoFilter.SOLARIZE -> gpuImage.setFilter(GPUImageSolarizeFilter())
                VideoFilter.VIGNETTE -> gpuImage.setFilter(GPUImageVignetteFilter())
                else -> gpuImage.setFilter(GPUImageFilter())
            }
        }
    }
}