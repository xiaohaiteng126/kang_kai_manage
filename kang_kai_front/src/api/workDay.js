import request from './request'

export const queryWorkDays = (params) => request.get('/work-days', { params })
export const batchSaveWorkDays = (data) => request.post('/work-days/batch', data)
