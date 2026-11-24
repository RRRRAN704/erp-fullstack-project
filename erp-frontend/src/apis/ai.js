import request from '@/utils/http'

// history: earlier turns, oldest first: [{ role: 'user' | 'assistant', content }]
export const chatAPI = (question, history) => {
  return request({
    url: 'ai/chat',
    method: 'POST',
    data: { question, history },
    timeout: 60000 // a question may take several model calls
  })
}
