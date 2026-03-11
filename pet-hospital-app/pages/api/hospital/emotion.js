// 情绪检测
export function detectEmotion(data) {
    return new Promise((resolve, reject) => {
        uni.request({
            url: 'http://localhost:5000/predict_base64',
            method: 'POST',
            data: data,
            header: {
                'Content-Type': 'application/json'
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    resolve(res.data)
                } else {
                    reject(new Error('请求失败'))
                }
            },
            fail: (err) => {
                reject(err)
            }
        })
    })
}
