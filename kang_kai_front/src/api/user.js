import request from './request'

export const listUsers = () => request.get('/users')
export const getUser = (id) => request.get(`/users/${id}`)
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/users/${id}`)
export const resetPassword = (id, data) => request.put(`/users/${id}/reset-password`, data)
