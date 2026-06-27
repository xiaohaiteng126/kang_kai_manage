import request from './request'

export const getStatistics = (params) => request.get('/statistics', { params })
