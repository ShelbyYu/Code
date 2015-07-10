package com.xueyu.application;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.lidroid.xutils.util.LogUtils;
import com.xueyu.util.FileUtil;
import android.app.Application;
import android.content.Context;

/**
 * @author 陈学宇
 * @version 创建时间：2015年3月12日 下午5:25:00
 */
public class MyApplication extends Application {

	private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();//分配的可用内存
	public static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 4;//使用的缓存数量

	//小图极低磁盘空间缓存的最大值（特性：可将大量的小图放到额外放在另一个磁盘空间防止大图占用磁盘空间而删除了大量的小图）
	public static final int MAX_SMALL_DISK_VERYLOW_CACHE_SIZE = 5 * ByteConstants.MB;

	//小图低磁盘空间缓存的最大值（特性：可将大量的小图放到额外放在另一个磁盘空间防止大图占用磁盘空间而删除了大量的小图）
	public static final int MAX_SMALL_DISK_LOW_CACHE_SIZE = 10 * ByteConstants.MB;

	//小图磁盘缓存的最大值（特性：可将大量的小图放到额外放在另一个磁盘空间防止大图占用磁盘空间而删除了大量的小图）
	public static final long MAX_SMALL_DISK_CACHE_SIZE = 20 * ByteConstants.MB;
	public static final long MAX_DISK_CACHE_VERYLOW_SIZE = 100 * ByteConstants.MB;//默认图极低磁盘空间缓存的最大值
	public static final long MAX_DISK_CACHE_LOW_SIZE = 500 * ByteConstants.MB;//默认图低磁盘空间缓存的最大值
	public static final long MAX_DISK_CACHE_SIZE = Long.MAX_VALUE-1;//默认图磁盘缓存的最大值


	private static final String IMAGE_PIPELINE_SMALL_CACHE_DIR = "imagepipeline_cache";//小图所放路径的文件夹名
	private static final String IMAGE_PIPELINE_CACHE_DIR = "ImageCache";//默认图所放路径的文件夹名

	@Override
	public void onCreate() {
		super.onCreate();
		Fresco.initialize(getApplicationContext(),configureCaches(getApplicationContext()));
		LogUtils.e("Fresco初始化了");
	}
	/**
	 * 初始化配置
	 */
	private static ImagePipelineConfig configureCaches(Context context) {
		final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
				MAX_MEMORY_CACHE_SIZE, // 内存缓存中总图片的最大大小,以字节为单位。
				Integer.MAX_VALUE, // 内存缓存中图片的最大数量。
				MAX_MEMORY_CACHE_SIZE, // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
				Integer.MAX_VALUE, // 内存缓存中准备清除的总图片的最大数量。
				Integer.MAX_VALUE); // 内存缓存中单个图片的最大大小。

		//修改内存图片缓存数量，空间策略（这个方式有点恶心）
		Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
			@Override
			public MemoryCacheParams get() {
				return bitmapCacheParams;
			}
		};

		//小图片的磁盘配置
		DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder()
				.setBaseDirectoryPath(context.getApplicationContext().getCacheDir())//缓存图片基路径
				.setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)//文件夹名
				.setMaxCacheSize(MAX_SMALL_DISK_CACHE_SIZE)//默认缓存的最大大小。
				.setMaxCacheSizeOnLowDiskSpace(MAX_SMALL_DISK_LOW_CACHE_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
				.setMaxCacheSizeOnVeryLowDiskSpace(MAX_SMALL_DISK_VERYLOW_CACHE_SIZE)//缓存的最大大小,当设备极低磁盘空间
			 // .setVersion(version)
				.build();

		//默认图片的磁盘配置
		DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
				.setBaseDirectoryPath(FileUtil.getCacheFile())//缓存图片基路径
				.setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//文件夹名
				.setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
				.setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
				.setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
			 // .setVersion(version)
				.build();

		//缓存图片配置
		ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context)
				.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)//内存缓存配置（一级缓存，已解码的图片）
				.setMainDiskCacheConfig(diskCacheConfig)//磁盘缓存配置（总，三级缓存）
				.setSmallImageDiskCacheConfig(diskSmallCacheConfig)//磁盘缓存配置（小图片，可选～三级缓存的小图优化缓存）
				;
		return configBuilder.build();
	}
}
