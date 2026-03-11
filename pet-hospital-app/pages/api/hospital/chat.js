import config from "@/config";

//流式对话接口
export function sseChat(memoryId, message) {
    const baseUrl = config.baseUrl

    return new Promise((resolve) => {
        //拼接路径
        const eventSource = new EventSource(
            `${baseUrl}/ai/chat/sse?message=${message}&memoryId=${memoryId}`
        )
        resolve(eventSource)
    })
}