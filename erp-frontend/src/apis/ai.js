import request from '@/utils/http'

export const getAiResponse = (q) => {
  return request({
    url: 'ai/ask',
    method: 'GET',
    params: { q }
  })
}
