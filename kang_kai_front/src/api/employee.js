import request from './request'

export const listEmployees = (params) => request.get('/employees', { params })
export const getEmployee = (id) => request.get(`/employees/${id}`)
export const createEmployee = (data) => request.post('/employees', data)
export const updateEmployee = (id, data) => request.put(`/employees/${id}`, data)
export const deleteEmployee = (id) => request.delete(`/employees/${id}`)
