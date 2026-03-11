<template>
    <view class="container">
        <!-- 上传区域 -->
        <view class="upload-section">
            <view class="section-title">
                <text>宠物情绪检测</text>
            </view>
            <view class="upload-desc">
                <text>上传您宠物的照片，深度算法将为您分析宠物的情绪状态</text>
            </view>

            <!-- 图片预览区域 -->
            <view class="image-preview" v-if="imageUrl">
                <image :src="imageUrl" mode="aspectFit" class="preview-image"/>
                <view class="remove-btn" @click.stop="removeImage">
                    <text>✕</text>
                </view>
            </view>

            <!-- 上传按钮 -->
            <view class="upload-btn" @click="chooseImage" v-if="!imageUrl">
                <text class="upload-emoji">📷</text>
                <text class="upload-text">点击上传照片</text>
            </view>

            <!-- 分析按钮 -->
            <button 
                class="analyze-btn" 
                @click="analyzeEmotion"
                :disabled="!imageUrl || isAnalyzing"
                :class="{ 'disabled': !imageUrl || isAnalyzing }"
            >
                <view v-if="!isAnalyzing">开始分析</view>
                <view v-else class="loading-wrapper">
                    <view class="loading-spinner"></view>
                    <text>分析中...</text>
                </view>
            </button>
        </view>

        <!-- 结果展示区域 -->
        <view class="result-section" v-if="emotionResult">
            <view class="section-title">
                <text>分析结果</text>
            </view>
            
            <!-- 主要情绪 -->
            <view class="main-emotion" :class="'emotion-' + emotionResult.emotion.toLowerCase()">
                <view class="emotion-icon">{{ getEmotionIcon(emotionResult.emotion) }}</view>
                <view class="emotion-name">{{ getEmotionName(emotionResult.emotion) }}</view>
                <view class="emotion-confidence">置信度: {{ (emotionResult.confidence * 100).toFixed(2) }}%</view>
            </view>

            <!-- 所有情绪得分 -->
            <view class="scores-section">
                <view class="scores-title">详细情绪分析</view>
                <view class="score-list">
                    <view 
                        v-for="(score, emotion) in emotionResult.all_scores" 
                        :key="emotion"
                        class="score-item"
                    >
                        <view class="score-header">
                            <text class="score-emotion">{{ getEmotionName(emotion) }}</text>
                            <text class="score-value">{{ (score * 100).toFixed(2) }}%</text>
                        </view>
                        <view class="score-bar-bg">
                            <view 
                                class="score-bar" 
                                :class="'bar-' + emotion.toLowerCase()"
                                :style="{ width: (score * 100) + '%' }"
                            ></view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref } from 'vue'
import { detectEmotion } from '@/pages/api/hospital/emotion'

const imageUrl = ref('')
const imageBase64 = ref('')
const emotionResult = ref(null)
const isAnalyzing = ref(false)

// 情绪名称映射
const emotionNames = {
    'Angry': '生气',
    'happy': '开心',
    'Sad': '伤心',
    'Other': '其他'
}

// 情绪图标映射
const emotionIcons = {
    'Angry': '😠',
    'happy': '😊',
    'Sad': '😢',
    'Other': '😐'
}

// 获取情绪名称
const getEmotionName = (emotion) => {
    return emotionNames[emotion] || emotion
}

// 获取情绪图标
const getEmotionIcon = (emotion) => {
    return emotionIcons[emotion] || '🐾'
}

// 选择图片
const chooseImage = () => {
    uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
            const tempFilePath = res.tempFilePaths[0]
            imageUrl.value = tempFilePath
            
            // #ifdef H5
            // H5环境下直接使用FileReader转换base64
            const xhr = new XMLHttpRequest()
            xhr.open('GET', tempFilePath, true)
            xhr.responseType = 'blob'
            xhr.onload = function() {
                if (this.status === 200) {
                    const reader = new FileReader()
                    reader.onloadend = function() {
                        // 移除data:image/xxx;base64,前缀
                        imageBase64.value = reader.result.split(',')[1]
                    }
                    reader.readAsDataURL(this.response)
                }
            }
            xhr.send()
            // #endif
            
            // #ifndef H5
            // 非H5环境使用文件系统管理器
            uni.getFileSystemManager().readFile({
                filePath: tempFilePath,
                encoding: 'base64',
                success: (fileRes) => {
                    imageBase64.value = fileRes.data
                },
                fail: (err) => {
                    console.error('读取文件失败:', err)
                    uni.showToast({
                        title: '图片读取失败',
                        icon: 'none'
                    })
                }
            })
            // #endif
        }
    })
}

// 移除图片
const removeImage = () => {
    imageUrl.value = ''
    imageBase64.value = ''
    emotionResult.value = null
}

// 分析情绪
const analyzeEmotion = async () => {
    if (!imageBase64.value) {
        uni.showToast({
            title: '请先上传图片',
            icon: 'none'
        })
        return
    }

    try {
        isAnalyzing.value = true
        emotionResult.value = null
        
        const res = await detectEmotion({
            image: imageBase64.value
        })
        
        if (res.success && res.data) {
            emotionResult.value = res.data
            uni.showToast({
                title: '分析完成',
                icon: 'success'
            })
        } else {
            throw new Error('分析失败')
        }
    } catch (error) {
        console.error('情绪检测失败:', error)
        uni.showToast({
            title: '分析失败，请重试',
            icon: 'none'
        })
    } finally {
        isAnalyzing.value = false
    }
}
</script>

<style scoped>
.container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
    padding: 30rpx;
}

.upload-section {
    background: white;
    border-radius: 20rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.section-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
}

.upload-desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 40rpx;
}

.image-preview {
    position: relative;
    width: 100%;
    height: 500rpx;
    border-radius: 16rpx;
    overflow: hidden;
    margin-bottom: 30rpx;
    background: #f8f9fa;
}

.preview-image {
    width: 100%;
    height: 100%;
}

.remove-btn {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    width: 60rpx;
    height: 60rpx;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 40rpx;
    font-weight: bold;
    z-index: 10;
}

.upload-btn {
    width: 100%;
    height: 300rpx;
    border: 2rpx dashed #d0d0d0;
    border-radius: 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    margin-bottom: 30rpx;
}

.upload-btn .upload-emoji {
    font-size: 80rpx;
    margin-bottom: 20rpx;
}

.upload-text {
    font-size: 28rpx;
    color: #666;
}

.analyze-btn {
    width: 100%;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 16rpx;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
}

.analyze-btn.disabled {
    background: #bdc3c7;
    opacity: 0.6;
}

.loading-wrapper {
    display: flex;
    align-items: center;
    gap: 20rpx;
}

.loading-spinner {
    width: 32rpx;
    height: 32rpx;
    border: 4rpx solid transparent;
    border-top: 4rpx solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.result-section {
    background: white;
    border-radius: 20rpx;
    padding: 40rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20rpx);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 主要情绪展示 */
.main-emotion {
    padding: 40rpx;
    border-radius: 16rpx;
    text-align: center;
    margin-top: 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.emotion-angry {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.emotion-happy {
    background: linear-gradient(135deg, #ffd93d 0%, #ffb347 100%);
}

.emotion-sad {
    background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
}

.emotion-other {
    background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
}

.emotion-icon {
    font-size: 100rpx;
    margin-bottom: 20rpx;
}

.emotion-name {
    font-size: 48rpx;
    font-weight: bold;
    color: white;
    margin-bottom: 10rpx;
}

.emotion-confidence {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
}

/* 详细得分区域 */
.scores-section {
    margin-top: 30rpx;
}

.scores-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
}

.score-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.score-item {
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
}

.score-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10rpx;
}

.score-emotion {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
}

.score-value {
    font-size: 26rpx;
    font-weight: bold;
    color: #666;
}

.score-bar-bg {
    height: 16rpx;
    background: #e0e0e0;
    border-radius: 8rpx;
    overflow: hidden;
}

.score-bar {
    height: 100%;
    border-radius: 8rpx;
    transition: width 0.5s ease;
}

.bar-angry {
    background: linear-gradient(90deg, #ff6b6b 0%, #ee5a6f 100%);
}

.bar-happy {
    background: linear-gradient(90deg, #ffd93d 0%, #ffb347 100%);
}

.bar-sad {
    background: linear-gradient(90deg, #6c5ce7 0%, #a29bfe 100%);
}

.bar-other {
    background: linear-gradient(90deg, #95a5a6 0%, #7f8c8d 100%);
}
</style>
